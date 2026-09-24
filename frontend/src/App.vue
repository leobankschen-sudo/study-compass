<script setup lang="ts">
import { NConfigProvider, NDialogProvider, NMessageProvider, zhCN, dateZhCN, type GlobalThemeOverrides } from 'naive-ui'

const themeOverrides: GlobalThemeOverrides = {
  common: {
    primaryColor: '#315c67',
    primaryColorHover: '#3f707c',
    primaryColorPressed: '#244954',
    primaryColorSuppl: '#6f8f86',
    infoColor: '#315c67',
    successColor: '#5f806f',
    warningColor: '#c59a5f',
    errorColor: '#a85751',
    borderRadius: '10px',
    fontFamily: 'Inter, "PingFang SC", "Microsoft YaHei", sans-serif',
  },
  Button: { borderRadiusMedium: '9px', fontWeight: '600' },
  Card: { borderRadius: '16px' },
}
</script>

<template>
  <n-config-provider :locale="zhCN" :date-locale="dateZhCN" :theme-overrides="themeOverrides">
    <n-message-provider>
      <n-dialog-provider><router-view /></n-dialog-provider>
    </n-message-provider>
  </n-config-provider>
</template>
