const { db } = require('../config/firebase.config');
const { BANK_CONFIG } = require('../config/sepay.config');

const createSepayPayment = async (orderId) => {
    try {
        const transId = `SEPAY-${Date.now()}-${orderId}`;
        const orderDoc = await db.collection('orders').doc(orderId).get();
        if (!orderDoc.exists) {
            throw new Error('Order not found');
        }
        const orderData = orderDoc.data();
        const total = orderData.total;
        const description = `SEVQR Thanh toán đơn hàng #${orderId}`;
        const desEncoded = encodeURIComponent(description);

        const qrUrl = `https://qr.sepay.vn/img?bank=${BANK_CONFIG.BANK_ID}&acc=${BANK_CONFIG.ACCOUNT_NO}&template=${BANK_CONFIG.TEMPLATE}&amount=${total}&des=${desEncoded}`;
        // const qrUrl = `https://qr.sepay.vn/img?bank=VCB&acc=1040829489&template=compact&amount=1000&des=Thanh_toán_đơn_hàng_abc123`;

        await db.collection('orders').doc(orderId).update({
            paymentMethod: 'SEPAY',
            status: 'WAITING_PAYMENT'
        })

        return {
            transId,
            qrUrl,
            amount: total,
            description
        };
    } catch (error) {
        throw new Error(`Failed to create SEPAY payment: ${error.message}`);
    }
}

const handleSepayWebhook = async (webhookData) => {
    try {
        const { content, transferAmount, id } = webhookData;
        console.log(">> Webhook Data:", content);

        let extractedOrderId = null;
        const sevqrIndex = content.indexOf('SEVQR');
        if (sevqrIndex !== -1) {
            const afterSevqr = content.substring(sevqrIndex + 'SEVQR'.length).trim();
            const parts = afterSevqr.split(/\s+/);
            const hangIndex = parts.findIndex(p => p.toLowerCase() === 'hang'); // tìm cái từ "hang" trong ["thanh", "toan", "don", "hang", "order_id"]
            if (hangIndex !== -1 && parts.length > hangIndex + 1) {
                extractedOrderId = parts[hangIndex + 1];
                if (extractedOrderId.endsWith('.CT')) {
                    extractedOrderId = extractedOrderId.slice(0, -3);
                }
            }
        }
        console.log(">> Extracted Order ID:", extractedOrderId);


        if (!extractedOrderId) {
            // Trường hợp không lấy được ID
            console.log("Không tìm thấy Order ID trong nội dung:", content);
            return { success: false, message: "Invalid content format" };
        }

        const order = await db.collection('orders').doc(extractedOrderId).get();
        if (!order.exists) {
            console.log("Không tìm thấy đơn hàng với ID:", extractedOrderId);
            return { success: false, message: "Order not found" };
        }

        const orderData = order.data();

        if (Number(orderData.total) !== Number(transferAmount)) {
            console.log(`Số tiền chuyển (${transferAmount}) không khớp với tổng đơn hàng (${orderData.total}) cho Order ID:`, extractedOrderId);
            return {
                success: false,
                message: "Amount mismatch"
            };
        }

        await db.collection('orders').doc(extractedOrderId).update({
            status: 'PENDING',
            paidAt: new Date(),
        });

        console.log(`Đơn hàng ${extractedOrderId} đã thanh toán thành công!`)
        return {
            success: true,
            message: "Payment processed successfully"
        };
    }
    catch (error) {
        console.error("Lỗi khi xử lý webhook SEPAY:", error);
        return {
            success: false,
            message: "Internal server error"
        };
    }
}

module.exports = {
    createSepayPayment,
    handleSepayWebhook
};