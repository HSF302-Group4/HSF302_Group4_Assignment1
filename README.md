- Nhánh: `feature/<số-issue>-desktop-<tên>` (ví dụ `feature/61-desktop-counter-sale`).
- Commit theo Conventional Commits, scope **`desktop`**:
  ```
  feat(desktop): add javafx dependencies and desktop launcher        Refs #<n>
  feat(desktop): add seat map component                              Refs #<n>
  fix(desktop): reload seat map after booking failure                Refs #<n>
  docs(readme): add desktop setup guide and screenshots              Refs #<n>
  ```
- PR mô tả: ảnh chụp màn hình desktop + các TC-D đã chạy.
- `CODEOWNERS`: thêm `/src/main/java/com/fu/cinema/desktop/` và `/src/main/resources/desktop/` → A; thay đổi `pom.xml` cần A duyệt.
- Không sửa file service/entity của người khác khi chưa có issue `module: infra` và người sở hữu cùng review.
