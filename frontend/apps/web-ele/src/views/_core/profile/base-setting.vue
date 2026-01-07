<script setup lang="ts">
import type { Recordable } from '@vben/types';

import type { VbenFormSchema } from '#/adapter/form';
import type { UpdateUserInfoParams } from '#/api/core/user';

import { computed, onMounted, ref } from 'vue';

import { ProfileBaseSetting, z } from '@vben/common-ui';
import { useUserStore } from '@vben/stores';
import { pickDefined } from '@vben/utils';

import { ElMessage } from 'element-plus';

import { getUserInfoApi, updateUserInfoApi } from '#/api/core/user';

const profileBaseSettingRef = ref();
const userStore = useUserStore();

const formSchema = computed((): VbenFormSchema[] => {
  return [
    {
      fieldName: 'username',
      component: 'Input',
      componentProps: {
        disabled: true,
      },
      label: '用户名',
      rules: z.string().optional(),
    },
    {
      fieldName: 'realName',
      component: 'Input',
      label: '真实姓名',
      rules: z
        .string()
        .max(50, { message: '真实姓名长度不能超过50' })
        .optional(),
    },
    {
      fieldName: 'nickname',
      component: 'Input',
      label: '昵称',
      rules: z.string().max(50, { message: '昵称长度不能超过50' }).optional(),
    },
    {
      fieldName: 'phone',
      component: 'Input',
      label: '手机号',
      rules: z
        .string()
        .optional()
        .refine((val) => !val || /^1[3-9]\d{9}$/.test(val), {
          message: '手机号格式不正确',
        }),
    },
    {
      fieldName: 'gender',
      component: 'RadioGroup',
      componentProps: {
        buttonStyle: 'solid',
        options: [
          { label: '未知', value: 0 },
          { label: '男', value: 1 },
          { label: '女', value: 2 },
        ],
        optionType: 'button',
      },
      defaultValue: 0,
      label: '性别',
      rules: z.number().min(0).max(2).optional(),
    },
  ];
});

onMounted(async () => {
  const data = await getUserInfoApi();
  // 使用类型断言，因为 UserInfo 类型可能不包含这些字段，但后端实际返回了
  const userData = data as Record<string, any>;
  profileBaseSettingRef.value.getFormApi().setValues({
    username: userData.username ?? '',
    realName: userData.realName ?? '',
    nickname: userData.nickname ?? '',
    phone: userData.phone ?? '',
    gender: userData.gender ?? 0,
  });
});

async function handleSubmit(values: Recordable<any>) {
  try {
    const formData = values as UpdateUserInfoParams;

    // 只提交有值的字段（过滤掉 undefined、null 和空字符串）
    const updateData = pickDefined(formData) as UpdateUserInfoParams;

    await updateUserInfoApi(updateData);

    ElMessage.success('基本信息更新成功');

    // 更新用户信息到 store
    const updatedUserInfo = await getUserInfoApi();
    userStore.setUserInfo(updatedUserInfo);
  } catch (error: unknown) {
    const errorMessage =
      error instanceof Error ? error.message : '基本信息更新失败，请重试';
    ElMessage.error(errorMessage);
  }
}
</script>
<template>
  <ProfileBaseSetting
    ref="profileBaseSettingRef"
    :form-schema="formSchema"
    @submit="handleSubmit"
  />
</template>
