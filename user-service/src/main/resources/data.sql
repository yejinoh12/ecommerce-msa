INSERT INTO user (created_at,
                  modified_at,
                  user_id,
                  address,
                  email,
                  name,
                  password,
                  phone,
                  role)
VALUES ('2024-09-03 15:23:25.132265',
        '2024-09-03 15:23:25.132265',
        1,
        'ATwObiwTWQ6kCqdSI6tCpg==',
        'mrPxpON822WNm+WXuDifSgAM7fgv1v8ANYRqi9hMq+w=',
        '1Hj7OBMNpNZGJJCHfW0sew==',
        '$2a$10$oSuqGxg7kH2NaqSReKYJteoymxYU020EyFtUUEa71ftkvEeCkOGd.',
        '6aqmiJp4daRz3aygsN/Gtg==',
        'USER');

INSERT INTO address (is_default, id, user_id, address, alias, detail_address, phone, postal_code)
VALUES
    (1, 1, 1, 'pwchYx0lj7zjX2J8XltH+g==', 'Home', 'K3wIj5rbNk3+BfSLyqxlog==', 'IQYO0jyhyCCx3ve4Ozs9cA==', 'y0qsRBTlJqf7YupqL77K4Q==');

INSERT INTO address (is_default, id, user_id, address, alias, detail_address, phone, postal_code)
VALUES
    (0, 2, 1, '3Fo1oHv6aEBOC/LEiiyJlA==', 'Office', 'K8vsVPhvKRnZKjuvqEVEkA==', '9ACTXEp0LCwDhe8a39cddg==', '1g0agEtkj3iJ+GaqnDZEdw==');
