# BÁO CÁO GIẢI TRÌNH: CƠ CHẾ BẢO MẬT ACCESS TOKEN VÀ REFRESH TOKEN

## 1. Phân Tích Sự Khác Biệt Cốt Lõi giữa Access Token và Refresh Token

Để tối ưu hóa giữa trải nghiệm người dùng (UX) và tính an toàn hệ thống, hai loại token này được thiết kế với các vai trò hoàn toàn biệt lập:

| Tiêu chí | Access Token | Refresh Token |
| :--- | :--- | :--- |
| **Mục đích** | Được đính kèm vào HTTP Header (`Authorization: Bearer <token>`) để **xác thực và ủy quyền** cho Client truy cập trực tiếp vào các tài nguyên bảo mật (Protected Resources) trên Server. | Chỉ có một nhiệm vụ duy nhất: Gửi lên Server để **cấp lại một cặp Access Token (và Refresh Token) mới** khi Access Token cũ đã hết hạn, giúp người dùng không phải đăng nhập lại. |
| **Vòng đời (Lifetime)** | **Ngắn** (Thường từ 5 - 15 phút). Hạn chế tối đa thiệt hại nếu vô tình bị lộ lọt. | **Dài** (Thường từ vài tuần đến vài tháng). Duy trì phiên đăng nhập hợp lệ cho người dùng. |
| **Lưu trữ an toàn (Client-side)** | - **Web:** Nên lưu trong **Memory (Biến Local/State)** để chống tấn công XSS, hoặc lưu trong `SessionStorage`. <br>- **Mobile (Android/iOS):** Lưu trong **SharedPreferences / EncryptedSharedPreferences** hoặc Keystore/Keychain. | - **Web:** Bắt buộc lưu trong **HttpOnly Cookie** với các cờ `Secure` và `SameSite=Strict` để ngăn chặn hoàn toàn mã độc Javascript đọc được (chống XSS). <br>- **Mobile:** Lưu biệt lập trong phân vùng mã hóa an toàn cao (Encrypted Storage). |

---

## 2. Quản Trị Rủi Ro Lộ Lọt và Cơ Chế Phòng Ngự DB (Cờ Revoked)

### 2.1 Rủi ro khi bị lộ Token
* **Nếu lộ Access Token:** Kẻ tấn công có thể mạo danh người dùng để thực hiện các hành động phá hoại. Tuy nhiên, rủi ro này mang **tính ngắn hạn** vì token sẽ tự động vô hiệu hóa sau vài phút. Do Access Token thường là Stateless (JWT), Server không cần check DB nên không thể chủ động thu hồi nó giữa chừng.
* **Nếu lộ Refresh Token:** Đây là rủi ro **nghiêm trọng mang tính dài hạn**. Kẻ tấn công có thể liên tục tạo ra các Access Token mới để thao túng tài khoản vô thời hạn, trừ khi hệ thống có cơ chế can thiệp ở tầng Database.



### 2.2 Cơ chế phòng ngự của hệ thống thông qua thực thể `UserToken` (Cờ `revoked`)
Để khắc phục điểm yếu "vô cảm" của Stateless JWT, hệ thống đã triển khai một Stateful Layer bằng bảng `UserToken` trong Database để lưu vết các Refresh Token đang hoạt động. Cờ `isRevoked` (hoặc `isExpired`) đóng vai trò là chốt chặn an ninh tối cao:

1. **Chủ động khóa phiên đăng nhập (Manual Revocation):** Khi người dùng ấn nút "Đăng xuất" hoặc thực hiện tính năng "Đăng xuất khỏi tất cả các thiết bị", hệ thống sẽ lập tức chuyển trạng thái của Refresh Token đó thành `isRevoked = true` trong DB. Khi kẻ tấn công cầm Refresh Token này đi gia hạn, hệ thống check DB thấy cờ `revoked` sẽ lập tức từ chối và chặn đứng chuỗi tấn công.
2. **Cơ chế luân chuyển (Refresh Token Rotation):** Mỗi lần người dùng đổi Refresh Token cũ lấy Access Token mới, hệ thống sẽ đánh dấu Refresh Token cũ là `isRevoked = true` và sinh ra một Refresh Token mới hoàn toàn.
3. **Phát hiện và phòng ngự tấn công Replay Attack:** Nếu kẻ tấn công đánh cắp được Refresh Token cũ và cố tình sử dụng lại, Server sẽ phát hiện ra mã token này đã bị `revoked`. Đây là dấu hiệu của một vụ hack. Hệ thống lập tức kích hoạt cơ chế tự vệ: **Vô hiệu hóa toàn bộ các Refresh Token hiện