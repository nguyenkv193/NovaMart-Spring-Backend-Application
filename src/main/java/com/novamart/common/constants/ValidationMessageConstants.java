package com.novamart.common.constants;

public final class ValidationMessageConstants {

    public static final String FIELD_REQUIRED = "ERR-002 - Trường dữ liệu không được để trống";
    public static final String FIELD_TOO_LONG = "ERR-003 - Trường dữ liệu không được vượt quá ký tự cho phép";
    public static final String FIELD_TOO_SHORT = "ERR-004 - Trường dữ liệu không được nhỏ hơn ký tự cho phép";
    public static final String EMAIL_INVALID = "ERR-005 - Email không hợp lệ";
    public static final String EMAIL_REQUIRED = "ERR-002 - Email không được để trống";
    public static final String PASSWORD_REQUIRED = "ERR-002 - Mật khẩu không được để trống";
    public static final String PASSWORD_TOO_SHORT = "ERR-004 - Mật khẩu phải có ít nhất 6 ký tự";
    public static final String FIRST_NAME_REQUIRED = "ERR-002 - Tên không được để trống";
    public static final String LAST_NAME_REQUIRED = "ERR-002 - Họ không được để trống";
    public static final String DATE_OF_BIRTH_REQUIRED = "ERR-002 - Ngày sinh không được để trống";
    public static final String DATE_OF_BIRTH_MUST_BE_IN_PAST = "ERR-005 - Ngày sinh phải ở trong quá khứ";
    public static final String ORDER_ITEMS_REQUIRED = "ERR-002 - Danh sách sản phẩm không được để trống";
    public static final String PRODUCT_ID_REQUIRED = "ERR-002 - Mã sản phẩm không được để trống";
    public static final String QUANTITY_REQUIRED = "ERR-002 - Số lượng không được để trống";
    public static final String QUANTITY_MUST_BE_POSITIVE = "ERR-004 - Số lượng phải lớn hơn 0";
    public static final String ORDER_STATUS_REQUIRED = "ERR-002 - Trạng thái đơn hàng không được để trống";

    private ValidationMessageConstants() {
    }
}
