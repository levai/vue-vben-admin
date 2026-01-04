<script lang="ts" setup>
import type { Recordable } from '@vben/types';

import type { OnActionClickParams } from '#/adapter/vxe-table';
import type { SystemUserApi } from '#/api/system/user';

import { ref } from 'vue';

import { Page, useVbenDrawer, useVbenModal } from '@vben/common-ui';
import { Plus } from '@vben/icons';

import {
  ElButton,
  ElInput,
  ElMessage,
  ElMessageBox,
  ElTag,
} from 'element-plus';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteUser,
  getUserList,
  resetUserPassword,
  updateUserStatus,
} from '#/api/system/user';
import { SYSTEM_PERMISSION_CODES } from '#/constants/permission-codes';
import { $t } from '#/locales';

import { useColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';

const [FormDrawer, formDrawerApi] = useVbenDrawer({
  connectedComponent: Form,
  destroyOnClose: true,
});

const [ResetPasswordModal, resetPasswordModalApi] = useVbenModal({
  title: $t('system.user.resetPassword'),
  class: 'w-[400px]',
});

const [Grid, gridApi] = useVbenVxeGrid<SystemUserApi.SystemUser>({
  formOptions: {
    fieldMappingTime: [['createTime', ['startTime', 'endTime']]],
    schema: useGridFormSchema(),
    submitOnChange: true,
  },
  gridOptions: {
    columns: useColumns(onActionClick, onStatusChange),
    height: 'auto',
    keepSource: true,
    scrollX: {
      enabled: true,
    },
    proxyConfig: {
      ajax: {
        query: async ({ page }: any, formValues: Recordable<any>) => {
          return await getUserList({
            page: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          });
        },
      },
    },
    rowConfig: {
      keyField: 'id',
    },

    toolbarConfig: {
      custom: true,
      export: false,
      refresh: true,
      search: true,
      zoom: true,
    },
  },
});

function onActionClick(e: OnActionClickParams<SystemUserApi.SystemUser>) {
  switch (e.code) {
    case 'delete': {
      onDelete(e.row);
      break;
    }
    case 'edit': {
      onEdit(e.row);
      break;
    }
    case 'resetPassword': {
      onResetPassword(e.row);
      break;
    }
  }
}

/**
 * 状态开关即将改变
 * @param newStatus 期望改变的状态值
 * @param row 行数据
 * @returns 返回false则中止改变，返回其他值（undefined、true）则允许改变
 */
async function onStatusChange(
  newStatus: number,
  row: SystemUserApi.SystemUser,
) {
  const status: Recordable<string> = {
    0: '禁用',
    1: '启用',
  };
  try {
    await ElMessageBox.confirm(
      `你要将用户 ${row.username} 的状态切换为 【${status[newStatus.toString()]}】 吗？`,
      `切换状态`,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      },
    );
    await updateUserStatus(row.id, newStatus as 0 | 1);
    return true;
  } catch {
    return false;
  }
}

function onEdit(row: SystemUserApi.SystemUser) {
  formDrawerApi.setData(row).open();
}

function onDelete(row: SystemUserApi.SystemUser) {
  ElMessage({
    duration: 1500,
    message: $t('ui.actionMessage.deleting', [row.username]),
  });
  deleteUser(row.id)
    .then(() => {
      ElMessage.closeAll();
      ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.username]));
      onRefresh();
    })
    .catch(() => {
      ElMessage.closeAll();
    });
}

function onResetPassword(row: SystemUserApi.SystemUser) {
  resetPasswordModalApi
    .setData({ userId: row.id, username: row.username })
    .open();
}

function onRefresh() {
  gridApi.query();
}

function onCreate() {
  formDrawerApi.setData({}).open();
}

const passwordRef = ref<string>('');

async function handleResetPassword() {
  const data = resetPasswordModalApi.getData<{
    userId: string;
    username: string;
  }>();
  if (!data || !passwordRef.value || passwordRef.value.length < 6) {
    ElMessage.error('密码长度不能少于6位');
    return;
  }

  resetPasswordModalApi.lock();
  try {
    await resetUserPassword(data.userId, passwordRef.value);
    ElMessage.success('密码重置成功');
    resetPasswordModalApi.close();
    passwordRef.value = '';
  } catch {
    resetPasswordModalApi.unlock();
  }
}
</script>
<template>
  <Page auto-content-height>
    <FormDrawer @success="onRefresh" />
    <ResetPasswordModal>
      <template #default>
        <div class="space-y-4">
          <div>
            <label class="mb-2 block">{{ $t('system.user.username') }}:</label>
            <ElInput
              :model-value="resetPasswordModalApi.getData()?.username"
              disabled
            />
          </div>
          <div>
            <label class="mb-2 block">
              {{ $t('system.user.newPassword') }}:
            </label>
            <ElInput
              v-model="passwordRef"
              :placeholder="$t('system.user.newPassword')"
              type="password"
            />
          </div>
        </div>
      </template>
      <template #footer>
        <div class="flex justify-end gap-2">
          <ElButton @click="resetPasswordModalApi.close()">取消</ElButton>
          <ElButton type="primary" @click="handleResetPassword">确定</ElButton>
        </div>
      </template>
    </ResetPasswordModal>
    <Grid :table-title="$t('system.user.list')">
      <template #toolbar-tools>
        <ElButton
          v-access:code="SYSTEM_PERMISSION_CODES.USER.ADD"
          type="primary"
          @click="onCreate"
        >
          <Plus class="size-5" />
          {{ $t('ui.actionTitle.create', [$t('system.user.name')]) }}
        </ElButton>
      </template>
      <template #roles="{ row }">
        <div
          v-if="row.roleNames && row.roleNames.length > 0"
          class="flex flex-wrap gap-1"
        >
          <ElTag v-for="roleName in row.roleNames" :key="roleName">
            {{ roleName }}
          </ElTag>
        </div>
        <span v-else class="text-gray-400">-</span>
      </template>
    </Grid>
  </Page>
</template>
