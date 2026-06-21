<template>
  <div class="side-decorations" aria-hidden="true">
    <!-- Left Side Decorations -->
    <div class="side left-side">
      <!-- Cat sitting -->
      <svg class="decor-cat" viewBox="0 0 200 240" xmlns="http://www.w3.org/2000/svg">
        <defs>
          <linearGradient id="catGrad" x1="0" y1="0" x2="1" y2="1">
            <stop offset="0%" stop-color="#FF8C42" />
            <stop offset="100%" stop-color="#FFB380" />
          </linearGradient>
        </defs>
        <!-- Body -->
        <ellipse cx="100" cy="175" rx="55" ry="45" fill="url(#catGrad)" opacity="0.85" />
        <!-- Head -->
        <circle cx="100" cy="105" r="42" fill="url(#catGrad)" opacity="0.85" />
        <!-- Left ear -->
        <polygon points="68,75 55,30 85,65" fill="url(#catGrad)" opacity="0.85" />
        <!-- Left ear inner -->
        <polygon points="70,72 62,40 82,66" fill="#FFD4BA" opacity="0.6" />
        <!-- Right ear -->
        <polygon points="132,75 145,30 115,65" fill="url(#catGrad)" opacity="0.85" />
        <!-- Right ear inner -->
        <polygon points="130,72 138,40 118,66" fill="#FFD4BA" opacity="0.6" />
        <!-- Eyes -->
        <ellipse cx="85" cy="100" rx="8" ry="10" fill="#4A3728" />
        <ellipse cx="115" cy="100" rx="8" ry="10" fill="#4A3728" />
        <circle cx="87" cy="97" r="3" fill="white" opacity="0.8" />
        <circle cx="117" cy="97" r="3" fill="white" opacity="0.8" />
        <!-- Nose -->
        <polygon points="100,110 96,115 104,115" fill="#FF6B6B" />
        <!-- Mouth -->
        <path d="M96 115 Q100 122 100 115" fill="none" stroke="#4A3728" stroke-width="1.5" />
        <path d="M104 115 Q100 122 100 115" fill="none" stroke="#4A3728" stroke-width="1.5" />
        <!-- Whiskers -->
        <line x1="50" y1="108" x2="78" y2="112" stroke="#4A3728" stroke-width="1" opacity="0.5" />
        <line x1="48" y1="118" x2="78" y2="117" stroke="#4A3728" stroke-width="1" opacity="0.5" />
        <line x1="122" y1="112" x2="150" y2="108" stroke="#4A3728" stroke-width="1" opacity="0.5" />
        <line x1="122" y1="117" x2="152" y2="118" stroke="#4A3728" stroke-width="1" opacity="0.5" />
        <!-- Tail -->
        <path d="M50 175 Q15 150 20 120 Q25 95 40 100" fill="none" stroke="url(#catGrad)" stroke-width="10" stroke-linecap="round" opacity="0.85" />
        <!-- Paws -->
        <ellipse cx="70" cy="215" rx="16" ry="8" fill="url(#catGrad)" opacity="0.85" />
        <ellipse cx="130" cy="215" rx="16" ry="8" fill="url(#catGrad)" opacity="0.85" />
      </svg>

      <!-- Paw prints -->
      <svg class="decor-paw paw-1" viewBox="0 0 60 80" xmlns="http://www.w3.org/2000/svg">
        <ellipse cx="30" cy="55" rx="18" ry="14" fill="#FF8C42" opacity="0.3" />
        <ellipse cx="14" cy="28" rx="8" ry="10" fill="#FF8C42" opacity="0.3" transform="rotate(-15 14 28)" />
        <ellipse cx="30" cy="18" rx="8" ry="10" fill="#FF8C42" opacity="0.3" />
        <ellipse cx="46" cy="28" rx="8" ry="10" fill="#FF8C42" opacity="0.3" transform="rotate(15 46 28)" />
      </svg>

      <svg class="decor-paw paw-2" viewBox="0 0 50 60" xmlns="http://www.w3.org/2000/svg">
        <ellipse cx="25" cy="42" rx="13" ry="10" fill="#FF8C42" opacity="0.2" />
        <ellipse cx="11" cy="22" rx="6" ry="7" fill="#FF8C42" opacity="0.2" transform="rotate(-15 11 22)" />
        <ellipse cx="25" cy="14" rx="6" ry="7" fill="#FF8C42" opacity="0.2" />
        <ellipse cx="39" cy="22" rx="6" ry="7" fill="#FF8C42" opacity="0.2" transform="rotate(15 39 22)" />
      </svg>

      <!-- Heart -->
      <svg class="decor-heart heart-left" viewBox="0 0 40 40" xmlns="http://www.w3.org/2000/svg">
        <path d="M20 34 C10 26 0 18 0 10 C0 4 5 0 10 0 C14 0 17 3 20 7 C23 3 26 0 30 0 C35 0 40 4 40 10 C40 18 30 26 20 34Z" fill="#FF6B6B" opacity="0.25" />
      </svg>
    </div>

    <!-- Right Side Decorations -->
    <div class="side right-side">
      <!-- Dog sitting -->
      <svg class="decor-dog" viewBox="0 0 200 240" xmlns="http://www.w3.org/2000/svg">
        <defs>
          <linearGradient id="dogGrad" x1="0" y1="0" x2="1" y2="1">
            <stop offset="0%" stop-color="#FF8C42" />
            <stop offset="100%" stop-color="#FFCC80" />
          </linearGradient>
        </defs>
        <!-- Body -->
        <ellipse cx="100" cy="170" rx="50" ry="42" fill="url(#dogGrad)" opacity="0.85" />
        <!-- Head -->
        <ellipse cx="100" cy="100" rx="40" ry="38" fill="url(#dogGrad)" opacity="0.85" />
        <!-- Left ear (floppy) -->
        <ellipse cx="65" cy="75" rx="18" ry="28" fill="#E07B30" opacity="0.7" transform="rotate(-20 65 75)" />
        <!-- Right ear (floppy) -->
        <ellipse cx="135" cy="75" rx="18" ry="28" fill="#E07B30" opacity="0.7" transform="rotate(20 135 75)" />
        <!-- Snout -->
        <ellipse cx="100" cy="118" rx="22" ry="16" fill="#FFD4BA" opacity="0.9" />
        <!-- Eyes -->
        <circle cx="84" cy="92" r="7" fill="#4A3728" />
        <circle cx="116" cy="92" r="7" fill="#4A3728" />
        <circle cx="86" cy="89" r="2.5" fill="white" opacity="0.8" />
        <circle cx="118" cy="89" r="2.5" fill="white" opacity="0.8" />
        <!-- Eyebrows -->
        <path d="M76 82 Q84 78 92 82" fill="none" stroke="#4A3728" stroke-width="2" opacity="0.5" />
        <path d="M108 82 Q116 78 124 82" fill="none" stroke="#4A3728" stroke-width="2" opacity="0.5" />
        <!-- Nose -->
        <ellipse cx="100" cy="112" rx="8" ry="6" fill="#4A3728" />
        <ellipse cx="100" cy="110" rx="3" ry="2" fill="white" opacity="0.3" />
        <!-- Mouth -->
        <path d="M92 120 Q100 128 100 120" fill="none" stroke="#4A3728" stroke-width="1.5" />
        <path d="M108 120 Q100 128 100 120" fill="none" stroke="#4A3728" stroke-width="1.5" />
        <!-- Tongue -->
        <ellipse cx="100" cy="128" rx="7" ry="9" fill="#FF6B6B" opacity="0.7" />
        <!-- Front legs -->
        <rect x="67" y="200" width="16" height="30" rx="8" fill="url(#dogGrad)" opacity="0.85" />
        <rect x="117" y="200" width="16" height="30" rx="8" fill="url(#dogGrad)" opacity="0.85" />
        <!-- Tail (wagging) -->
        <path d="M150 160 Q175 140 170 110 Q165 85 155 90" fill="none" stroke="url(#dogGrad)" stroke-width="9" stroke-linecap="round" opacity="0.85" />
      </svg>

      <!-- Bone -->
      <svg class="decor-bone" viewBox="0 0 80 30" xmlns="http://www.w3.org/2000/svg">
        <path d="M8 15 Q8 2 16 8 L64 8 Q72 2 72 15 Q72 28 64 22 L16 22 Q8 28 8 15Z" fill="#FF8C42" opacity="0.2" />
      </svg>

      <!-- Paw prints -->
      <svg class="decor-paw paw-3" viewBox="0 0 55 70" xmlns="http://www.w3.org/2000/svg">
        <ellipse cx="28" cy="48" rx="15" ry="12" fill="#FF8C42" opacity="0.25" />
        <ellipse cx="13" cy="25" rx="7" ry="9" fill="#FF8C42" opacity="0.25" transform="rotate(-15 13 25)" />
        <ellipse cx="28" cy="16" rx="7" ry="9" fill="#FF8C42" opacity="0.25" />
        <ellipse cx="43" cy="25" rx="7" ry="9" fill="#FF8C42" opacity="0.25" transform="rotate(15 43 25)" />
      </svg>

      <svg class="decor-paw paw-4" viewBox="0 0 45 55" xmlns="http://www.w3.org/2000/svg">
        <ellipse cx="22" cy="38" rx="12" ry="9" fill="#FF8C42" opacity="0.18" />
        <ellipse cx="10" cy="20" rx="5" ry="7" fill="#FF8C42" opacity="0.18" transform="rotate(-15 10 20)" />
        <ellipse cx="22" cy="13" rx="5" ry="7" fill="#FF8C42" opacity="0.18" />
        <ellipse cx="34" cy="20" rx="5" ry="7" fill="#FF8C42" opacity="0.18" transform="rotate(15 34 20)" />
      </svg>

      <!-- Heart -->
      <svg class="decor-heart heart-right" viewBox="0 0 40 40" xmlns="http://www.w3.org/2000/svg">
        <path d="M20 34 C10 26 0 18 0 10 C0 4 5 0 10 0 C14 0 17 3 20 7 C23 3 26 0 30 0 C35 0 40 4 40 10 C40 18 30 26 20 34Z" fill="#FF6B6B" opacity="0.2" />
      </svg>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.side-decorations {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
}

.side {
  position: absolute;
  top: 60px;
  bottom: 0;
  width: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 60px;
}

.left-side {
  left: 0;
}

.right-side {
  right: 0;
}

/* Cat decoration */
.decor-cat {
  width: 120px;
  height: auto;
  opacity: 0.9;
  filter: drop-shadow(0 4px 12px rgba(255, 140, 66, 0.15));
  animation: floatCat 6s ease-in-out infinite;
}

@keyframes floatCat {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  25% { transform: translateY(-6px) rotate(-1deg); }
  75% { transform: translateY(-10px) rotate(1deg); }
}

/* Dog decoration */
.decor-dog {
  width: 120px;
  height: auto;
  opacity: 0.9;
  filter: drop-shadow(0 4px 12px rgba(255, 140, 66, 0.15));
  animation: floatDog 7s ease-in-out infinite;
}

@keyframes floatDog {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  30% { transform: translateY(-8px) rotate(1deg); }
  70% { transform: translateY(-12px) rotate(-1deg); }
}

/* Paw prints */
.decor-paw {
  position: absolute;
  width: 40px;
  height: auto;
}

.paw-1 {
  left: 30px;
  top: 55%;
  animation: pawFade1 5s ease-in-out infinite;
}

.paw-2 {
  left: 130px;
  top: 75%;
  animation: pawFade2 6s ease-in-out infinite 1s;
}

.paw-3 {
  right: 20px;
  top: 50%;
  animation: pawFade3 5.5s ease-in-out infinite 0.5s;
}

.paw-4 {
  right: 130px;
  top: 78%;
  animation: pawFade4 6.5s ease-in-out infinite 1.5s;
}

@keyframes pawFade1 {
  0%, 100% { opacity: 0.3; transform: scale(1) rotate(0deg); }
  50% { opacity: 0.5; transform: scale(1.1) rotate(5deg); }
}

@keyframes pawFade2 {
  0%, 100% { opacity: 0.18; transform: scale(1) rotate(5deg); }
  50% { opacity: 0.35; transform: scale(1.08) rotate(-5deg); }
}

@keyframes pawFade3 {
  0%, 100% { opacity: 0.22; transform: scale(1) rotate(-3deg); }
  50% { opacity: 0.4; transform: scale(1.1) rotate(3deg); }
}

@keyframes pawFade4 {
  0%, 100% { opacity: 0.15; transform: scale(1) rotate(8deg); }
  50% { opacity: 0.3; transform: scale(1.05) rotate(-8deg); }
}

/* Bone */
.decor-bone {
  position: absolute;
  right: 60px;
  top: 65%;
  width: 60px;
  height: auto;
  animation: boneFloat 8s ease-in-out infinite 2s;
}

@keyframes boneFloat {
  0%, 100% { opacity: 0.18; transform: translateY(0) rotate(-10deg); }
  50% { opacity: 0.3; transform: translateY(-8px) rotate(-5deg); }
}

/* Hearts */
.decor-heart {
  position: absolute;
  width: 25px;
  height: auto;
}

.heart-left {
  left: 100px;
  top: 40%;
  animation: heartFloat 4s ease-in-out infinite 0.5s;
}

.heart-right {
  right: 80px;
  top: 35%;
  animation: heartFloat 5s ease-in-out infinite 1s;
}

@keyframes heartFloat {
  0%, 100% { opacity: 0.15; transform: translateY(0) scale(1); }
  50% { opacity: 0.3; transform: translateY(-12px) scale(1.15); }
}

/* Large screen optimization */
@media (min-width: 1920px) {
  .side {
    width: 260px;
  }
  .decor-cat, .decor-dog {
    width: 150px;
  }
  .decor-paw {
    width: 50px;
  }
  .decor-bone {
    width: 70px;
  }
  .decor-heart {
    width: 30px;
  }
}

/* Medium screens - hide decorations */
@media (max-width: 1400px) {
  .side-decorations {
    display: none;
  }
}
</style>
（内容由AI生成，仅供参考）
