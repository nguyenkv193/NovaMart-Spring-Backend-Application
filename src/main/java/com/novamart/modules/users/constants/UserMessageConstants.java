package com.novamart.modules.users.constants;

public final class UserMessageConstants {

    public static final String USER_CREATED_SUCCESSFULLY =
            "SUC-006 - Tạo người dùng thành công";

    public static final String USER_UPDATED_SUCCESSFULLY =
            "SUC-007 - Cập nhật người dùng thành công";

    public static final String USER_DELETED_SUCCESSFULLY =
            "SUC-008 - Xóa người dùng thành công";

    public static final String USER_FOUND_SUCCESSFULLY =
            "SUC-009 - Tìm thấy người dùng với id %d";

    public static final String USERS_FETCHED_SUCCESSFULLY =
            "SUC-010 - Lấy danh sách người dùng thành công";

    public static final String CURRENT_USER_FETCHED_SUCCESSFULLY =
            "SUC-011 - Lấy thông tin người dùng hiện tại thành công";

    public static final String USER_FOUND_BY_EMAIL_SUCCESSFULLY =
            "SUC-012 - Tìm thấy người dùng với email %s";

    public static final String USER_NOT_FOUND =
            "ERR-005 - Không tìm thấy người dùng với id %d";

    public static final String EMAIL_ALREADY_EXISTS =
            "ERR-006 - Email đã tồn tại";

    public static final String EMAIL_NOT_VALID =
            "ERR-005 - Email không hợp lệ";

    public static final String EMAIL_NOT_FOUND =
            "ERR-005 - Không tìm thấy người dùng với email %s";

    private UserMessageConstants() {
    }
}
