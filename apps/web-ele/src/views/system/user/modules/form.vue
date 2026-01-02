<script lang="ts" setup>
import type { SystemUserApi } from '#/api/system/user';

import { computed, nextTick, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';

import { useVbenForm } from '#/adapter/form';
import { getRoleOptions } from '#/api/system/role';
import { createUser, updateUser } from '#/api/system/user';
import { $t } from '#/locales';

import { useFormSchema } from '../data';

const emits = defineEmits(['success']);

const formData = ref<SystemUserApi.SystemUser>();

const roleOptions = ref<Array<{ label: string; value: string }>>([]);
const loadingRoles = ref(false);

const id = ref<string>();

// 先定义 schema，后续会更新角色选项
const [Form, formApi] = useVbenForm({
  schema: useFormSchema(false),
  showDefaultActions: false,
});

const [Drawer, drawerApi] = useVbenDrawer({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;
    const values = await formApi.getValues();

    // 处理角色数据：将 roleIds 转换为后端需要的格式
    const submitData: any = {
      ...values,
      roleIds: values.roleIds || [],
    };

    // 如果没有提供密码，则不传密码字段（新增时后端会使用默认密码，更新时不修改密码）
    if (!values.password) {
      delete submitData.password;
    }

    drawerApi.lock();
    try {
      await (id.value
        ? updateUser(id.value, submitData)
        : createUser(submitData));
      emits('success');
      drawerApi.close();
    } catch {
      drawerApi.unlock();
    }
  },

  async onOpenChange(isOpen) {
    if (isOpen) {
      const data = drawerApi.getData<SystemUserApi.SystemUser>();
      formApi.resetForm();

      const isEdit = !!(data && data.id);
      if (isEdit) {
        formData.value = data;
        id.value = data.id;
      } else {
        id.value = undefined;
        formData.value = undefined;
      }

      // 加载角色列表（必须在更新 schema 之前加载）
      if (roleOptions.value.length === 0) {
        await loadRoles();
      }

      // 更新 schema（编辑时密码可选），并设置角色选项
      const schema = useFormSchema(isEdit);
      const roleField = schema.find((s) => s.fieldName === 'roleIds');
      if (roleField && roleOptions.value.length > 0) {
        roleField.componentProps = {
          ...roleField.componentProps,
          options: roleOptions.value,
        };
      }
      formApi.updateSchema(schema);

      // Wait for Vue to flush DOM updates (form fields mounted)
      await nextTick();

      if (data) {
        // 设置表单值，将 roles 转换为 roleIds
        const formValues: any = {
          ...data,
          roleIds: data.roles || [],
        };
        // 更新时不传密码字段
        if (isEdit) {
          delete formValues.password;
        }
        formApi.setValues(formValues);
      }
    }
  },
});

async function loadRoles() {
  loadingRoles.value = true;
  try {
    const res = await getRoleOptions({ status: 1 });
    const roles = res.items || [];
    roleOptions.value = roles.map((role: any) => ({
      label: role.name,
      value: role.id,
    }));
  } finally {
    loadingRoles.value = false;
  }
}

// 初始化时加载角色列表
loadRoles();

const getDrawerTitle = computed(() => {
  return formData.value?.id
    ? $t('common.edit', $t('system.user.name'))
    : $t('common.create', $t('system.user.name'));
});
</script>
<template>
  <Drawer :title="getDrawerTitle">
    <Form />
  </Drawer>
</template>
