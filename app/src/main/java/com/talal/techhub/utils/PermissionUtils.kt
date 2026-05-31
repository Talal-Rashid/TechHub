package com.talal.techhub.utils

import com.talal.techhub.models.User

object PermissionUtils {

    fun isMasterAdmin(user: User): Boolean {
        return user.role == "master_admin"
    }

    fun isSubAdmin(user: User): Boolean {
        return user.role == "sub_admin"
    }

    fun hasPermission(user: User, permission: String): Boolean {
        if (isMasterAdmin(user)) return true
        return user.permissions[permission] == true
    }

    const val MANAGE_VENDORS = "canManageVendors"
    const val MANAGE_PRODUCTS = "canManageProducts"
    const val MANAGE_ORDERS = "canManageOrders"
    const val MANAGE_PC_BUILDS = "canManagePcBuilds"
    const val MANAGE_USERS = "canManageUsers"
    const val HIDE_PRODUCTS = "canHideProducts"
}