-- ============================================
-- PawMatch 知识百科库 + 用户成长激励体系
-- ============================================

-- 模块一：知识百科库
CREATE TABLE IF NOT EXISTS wiki_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    parent_id BIGINT DEFAULT NULL,
    sort_order INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES wiki_category(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS wiki_entry (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    summary VARCHAR(500) DEFAULT '',
    content TEXT,
    category_id BIGINT,
    status TINYINT DEFAULT 1 COMMENT '1=已发布 0=草稿 2=待审核',
    author_id BIGINT NOT NULL,
    view_count INT DEFAULT 0,
    helpful_count INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES wiki_category(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS wiki_revision (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entry_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content_before TEXT,
    content_after TEXT,
    summary VARCHAR(200) DEFAULT '',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (entry_id) REFERENCES wiki_entry(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS wiki_contribution (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    entry_id BIGINT NOT NULL,
    contrib_type VARCHAR(20) NOT NULL COMMENT 'create/edit',
    points_awarded INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (entry_id) REFERENCES wiki_entry(id) ON DELETE CASCADE
);

-- 预置百科分类
INSERT INTO wiki_category (name, parent_id, sort_order) VALUES
('养宠入门', NULL, 1),
('猫', NULL, 2),
('狗', NULL, 3),
('其他宠物', NULL, 4),
('健康与医疗', NULL, 5),
('行为训练', NULL, 6),
('营养与饮食', NULL, 7),
('领养指南', NULL, 8);

-- 子分类
INSERT INTO wiki_category (name, parent_id, sort_order) VALUES
('新手准备', 1, 1),
('日常护理', 1, 2),
('猫咪品种', 2, 1),
('猫咪行为', 2, 2),
('狗狗品种', 3, 1),
('狗狗训练', 3, 2),
('兔子', 4, 1),
('仓鼠', 4, 2),
('鸟类', 4, 3),
('常见疾病', 5, 1),
('疫苗接种', 5, 2),
('紧急处理', 5, 3),
('基础指令', 6, 1),
('行为纠正', 6, 2),
('猫粮选择', 7, 1),
('狗粮选择', 7, 2),
('领养流程', 8, 1),
('领养故事', 8, 2);

CREATE TABLE IF NOT EXISTS wiki_helpful_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entry_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (entry_id) REFERENCES wiki_entry(id) ON DELETE CASCADE,
    UNIQUE KEY uk_entry_user (entry_id, user_id)
);



-- 模块二：用户成长激励体系
CREATE TABLE IF NOT EXISTS badge (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(30) NOT NULL,
    description VARCHAR(200) DEFAULT '',
    icon VARCHAR(20) DEFAULT '🏅',
    badge_type VARCHAR(20) NOT NULL COMMENT 'adoption/knowledge/community/shelter/loyalty',
    unlock_condition VARCHAR(200) DEFAULT '',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_badge (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    badge_id BIGINT NOT NULL,
    awarded_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (badge_id) REFERENCES badge(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_badge (user_id, badge_id)
);

CREATE TABLE IF NOT EXISTS user_points_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    points INT NOT NULL COMMENT '正数为加，负数为扣',
    action VARCHAR(30) NOT NULL,
    description VARCHAR(200) DEFAULT '',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_time (user_id, create_time)
);

-- 预置徽章（8个）
INSERT INTO badge (name, description, icon, badge_type, unlock_condition) VALUES
('新晋铲屎官', '首次领养成功', '🐱', 'adoption', '首次领养申请通过并完成领养'),
('百科达人', '贡献10条知识卡被收录', '📚', 'knowledge', '累计创建或编辑百科词条被收录达到10条'),
('热心肠', '回答被采纳20次', '💝', 'community', '社区评论被采纳或认可达到20次'),
('社区元老', '注册满365天', '👑', 'loyalty', '注册时间满365天'),
('救助站之光', '成功送出50只宠物', '🏠', 'shelter', '救助站累计成功领养达到50只'),
('满分信用', '信用分达到100', '⭐', 'adoption', '信用评分达到100分'),
('爆款作者', '帖子被浏览1000次', '🔥', 'community', '发布的帖子累计浏览量达到1000次'),
('铲屎百事通', '百科被标记有帮助50次', '🎓', 'knowledge', '创作的百科词条累计被标记有帮助达到50次');