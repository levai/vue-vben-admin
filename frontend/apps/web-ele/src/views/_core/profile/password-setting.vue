<script setup lang="ts">
import type { Recordable } from '@vben/types';

import type { VbenFormSchema } from '#/adapter/form';

import { computed, ref } from 'vue';

import { ProfilePasswordSetting, z } from '@vben/common-ui';
import { useAccessStore } from '@vben/stores';

import { ElMessage, ElMessageBox } from 'element-plus';

import { changePasswordApi } from '#/api/core/user';

/**
 * 密码表单值类型
 */
type PasswordFormValues = {
  /** 确认密码 */
  confirmPassword: string;
  /** 新密码 */
  newPassword: string;
  /** 旧密码 */
  oldPassword: string;
};

const profilePasswordSettingRef = ref();
const accessStore = useAccessStore();

const formSchema = computed((): VbenFormSchema[] => {
  return [
    {
      fieldName: 'oldPassword',
      label: '旧密码',
      component: 'VbenInputPassword',
      componentProps: {
        placeholder: '请输入旧密码',
      },
      rules: z.string().min(1, { message: '请输入旧密码' }),
    },
    {
      fieldName: 'newPassword',
      label: '新密码',
      component: 'VbenInputPassword',
      componentProps: {
        passwordStrength: true,
        placeholder: '请输入新密码',
      },
      rules: z
        .string()
        .min(1, { message: '请输入新密码' })
        .min(6, { message: '新密码长度不能少于6位' })
        .max(50, { message: '新密码长度不能超过50位' })
        .refine(
          (value) => {
            // 至少包含数字和字母
            const hasNumber = /\d/.test(value);
            const hasLetter = /[a-z]/i.test(value);
            return hasNumber && hasLetter;
          },
          {
            message: '新密码必须包含数字和字母',
          },
        ),
    },
    {
      fieldName: 'confirmPassword',
      label: '确认密码',
      component: 'VbenInputPassword',
      componentProps: {
        passwordStrength: true,
        placeholder: '请再次输入新密码',
      },
      dependencies: {
        rules(values) {
          const { newPassword } = values;
          return z
            .string({ required_error: '请再次输入新密码' })
            .min(1, { message: '请再次输入新密码' })
            .refine((value) => value === newPassword, {
              message: '两次输入的密码不一致',
            });
        },
        triggerFields: ['newPassword'],
      },
    },
  ];
});

async function handleSubmit(values: Recordable<any>) {
  try {
    // 类型断言：表单验证已确保字段存在且类型正确
    const { newPassword, oldPassword } = values as PasswordFormValues;

    await changePasswordApi({
      newPassword,
      oldPassword,
    });

    // 密码修改成功后，提示用户需要重新登录
    ElMessageBox.confirm('为了账户安全，请使用新密码重新登录', '密码修改成功', {
      confirmButtonText: '立即登录',
      cancelButtonText: '稍后登录',
      type: 'success',
    })
      .then(() => {
        // 清除 token，显示登录弹窗
        accessStore.setAccessToken(null);
        accessStore.setLoginExpired(true);
      })
      .catch(() => {
        // 用户选择稍后登录，只重置表单
        profilePasswordSettingRef.value?.getFormApi()?.resetForm();
      });
  } catch (error: unknown) {
    const errorMessage =
      error instanceof Error ? error.message : '密码修改失败，请重试';
    ElMessage.error(errorMessage);
  }
}
</script>
<template>
  <ProfilePasswordSetting
    ref="profilePasswordSettingRef"
    class="w-1/3"
    :form-schema="formSchema"
    @submit="handleSubmit"
  />
</template>
