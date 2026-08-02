package com.novamart.modules.orders.constants;

public final class OrderMessageConstants {

    public static final String ORDER_CREATED_SUCCESSFULLY =
            "SUC-015 - Tạo đơn hàng thành công";

    public static final String ORDERS_FETCHED_SUCCESSFULLY =
            "SUC-016 - Lấy danh sách đơn hàng thành công";

    public static final String ORDER_FOUND_SUCCESSFULLY =
            "SUC-017 - Lấy thông tin đơn hàng thành công";

    public static final String ORDER_STATUS_UPDATED_SUCCESSFULLY =
            "SUC-018 - Cập nhật trạng thái đơn hàng thành công";

    public static final String ORDER_CANCELLED_SUCCESSFULLY =
            "SUC-019 - Hủy đơn hàng thành công";

    public static final String ORDER_NOT_FOUND =
            "ERR-008 - Không tìm thấy đơn hàng với id %d";

    public static final String INSUFFICIENT_STOCK =
            "ERR-009 - Sản phẩm với id %d không đủ tồn kho (còn %d, yêu cầu %d)";

    public static final String DUPLICATE_PRODUCT =
            "ERR-010 - Sản phẩm với id %d bị lặp trong đơn hàng";

    public static final String INVALID_STATUS_TRANSITION =
            "ERR-011 - Không thể chuyển đơn hàng với id %d từ trạng thái %s sang %s";

    public static final String CANCELLATION_NOT_ALLOWED =
            "ERR-012 - Chỉ có thể hủy đơn hàng ở trạng thái PENDING";

    private OrderMessageConstants() {
    }
}
