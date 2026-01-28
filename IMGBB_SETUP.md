# Hướng dẫn cấu hình ImgBB API

## Bước 1: Lấy ImgBB API Key miễn phí

1. Truy cập trang web ImgBB API: https://api.imgbb.com/
2. Nhấn vào nút **"Get API Key"** hoặc **"Sign up"**
3. Đăng ký tài khoản miễn phí (có thể dùng email hoặc đăng nhập qua Facebook/Google)
4. Sau khi đăng nhập, bạn sẽ thấy API key của mình ngay trên dashboard
5. Copy API key (dạng: `abc123def456...`)

## Bước 2: Cấu hình API Key trong dự án

1. Mở file `local.properties` trong thư mục gốc của dự án
2. Tìm dòng:
   ```
   imgbb.api.key=YOUR_IMGBB_API_KEY_HERE
   ```
3. Thay `YOUR_IMGBB_API_KEY_HERE` bằng API key bạn vừa copy
4. Ví dụ:
   ```
   imgbb.api.key=abc123def456ghi789jkl
   ```
5. Lưu file

## Bước 3: Sync Gradle

1. Mở Android Studio
2. Nhấn **"Sync Now"** khi có thông báo, hoặc chọn **File > Sync Project with Gradle Files**
3. Đợi quá trình sync hoàn tất

## Bước 4: Chạy ứng dụng

1. Build và chạy ứng dụng như bình thường
2. Tính năng upload avatar sẽ hoạt động với ImgBB

## Lưu ý quan trọng

- **KHÔNG** commit file `local.properties` lên Git (file này đã được thêm vào `.gitignore`)
- **KHÔNG** chia sẻ API key của bạn với người khác
- ImgBB free tier cho phép:
  - Upload không giới hạn số lượng ảnh
  - Băng thông không giới hạn
  - Lưu trữ vĩnh viễn
  - Kích thước file tối đa: 32MB

## Khắc phục sự cố

### Lỗi "Upload ảnh thất bại"
- Kiểm tra kết nối internet
- Đảm bảo API key đã được cấu hình đúng
- Kiểm tra kích thước ảnh (không quá 32MB)

### Lỗi "BuildConfig cannot be resolved"
- Chắc chắn đã enable `buildConfig = true` trong `build.gradle.kts`
- Sync Gradle lại
- Clean và Rebuild project

### Ảnh không hiển thị sau khi upload
- Kiểm tra URL trả về từ ImgBB (có thể log ra console)
- Đảm bảo Coil dependency đã được thêm vào project
- Kiểm tra kết nối internet khi load ảnh


