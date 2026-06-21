/**
 * WebSocket (STOMP over SockJS) 连接管理模块
 *
 * 职责：
 *   - 建立与后端 /ws 端点的 STOMP 长连接
 *   - 订阅 /user/queue/messages 私有频道，接收实时推送
 *   - 支持断线自动重连、token 更新
 *   - 通过回调机制向各组件分发收到的消息
 */

import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

class WebSocketManager {
  constructor() {
    this.client = null
    this.callbacks = []       // 消息回调列表
    this.subscription = null  // STOMP 订阅句柄
    this._isConnected = false
    this._reconnectAttempts = 0
    this._maxReconnectAttempts = 30
  }

  /**
   * 建立 WebSocket 连接并订阅用户私有频道
   */
  connect() {
    const token = sessionStorage.getItem('token')
    if (!token) return

    // 避免重复连接
    if (this.client && this.client.active) {
      return
    }

    const userInfo = JSON.parse(sessionStorage.getItem('userInfo') || '{}')

    this.client = new Client({
      // SockJS 作为 WebSocket 工厂，自动处理浏览器不支持 WebSocket 的场景
      webSocketFactory: () => new SockJS('/ws'),

      // STOMP CONNECT 帧的认证头：后端 ChannelInterceptor 通过 accessor.getLogin() 读取
      connectHeaders: {
        login: token,
        passcode: ''
      },

      // 心跳间隔（毫秒）
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,

      // 断线自动重连
      reconnectDelay: 5000,

      // 调试日志：生产环境可关闭
      debug: (str) => {
        if (process.env.NODE_ENV === 'development') {
          console.debug('[WS]', str)
        }
      },

      onConnect: (frame) => {
        this._isConnected = true
        this._reconnectAttempts = 0

        // 订阅用户私有消息频道
        this.subscription = this.client.subscribe(
          '/user/queue/messages',
          (message) => {
            try {
              const data = JSON.parse(message.body)
              this._dispatch(data)
            } catch (e) {
              console.error('[WS] 消息解析失败:', e)
            }
          }
        )
      },

      onDisconnect: () => {
        this._isConnected = false
      },

      onStompError: (frame) => {
        console.error('[WS] STOMP 错误:', frame.headers['message'])
        // token 过期场景：清除连接让用户重新登录后自动重连
        if (frame.headers['message'] && frame.headers['message'].includes('token')) {
          this.disconnect()
        }
      },

      onWebSocketClose: () => {
        this._isConnected = false
      }
    })

    this.client.activate()
  }

  /**
   * 断开连接（组件卸载时调用）
   */
  disconnect() {
    if (this.subscription) {
      this.subscription.unsubscribe()
      this.subscription = null
    }
    if (this.client) {
      this.client.deactivate()
      this.client = null
    }
    this._isConnected = false
  }

  /**
   * 注册消息回调
   * @param {Function} callback - 收到推送消息时调用，参数为解析后的消息对象
   */
  onMessage(callback) {
    if (typeof callback === 'function' && !this.callbacks.includes(callback)) {
      this.callbacks.push(callback)
    }
  }

  /**
   * 移除消息回调
   */
  offMessage(callback) {
    this.callbacks = this.callbacks.filter(cb => cb !== callback)
  }

  /**
   * 连接是否活跃
   */
  get isConnected() {
    return this._isConnected
  }

  // ---------- 内部方法 ----------

  /**
   * 向所有注册的回调分发消息
   */
  _dispatch(data) {
    this.callbacks.forEach(cb => {
      try {
        cb(data)
      } catch (e) {
        console.error('[WS] 回调执行出错:', e)
      }
    })
  }
}

// 单例导出
const wsManager = new WebSocketManager()
export default wsManager
