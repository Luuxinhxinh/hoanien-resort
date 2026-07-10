sql = "\n\n-- FIX GUEST REQUEST TASK OPERATIONAL TYPE\n"
sql += "UPDATE Hotel_Operations SET operational_type = 'GUEST_REQUEST' WHERE operational_type = 'CHECKOUT_CLEAN' AND notes LIKE '%[Khách Yêu Cầu Dọn Phòng]%';\n"

with open('d:\\SWP391\\su26-swp391-se2023-g2\\05-Development\\kawai-backend\\src\\main\\resources\\data.sql', 'a', encoding='utf-8') as f:
    f.write(sql)
