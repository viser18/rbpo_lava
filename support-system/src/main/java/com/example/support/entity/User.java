{
        "info": {
        "_postman_id": "support-system-complete",
        "name": "Support System - Complete Collection",
        "description": "Полная коллекция для тестирования Support System с CSRF и Basic Auth",
        "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
        },
        "item": [
        {
        "name": "1. ИНИЦИАЛИЗАЦИЯ (ВЫПОЛНИТЬ ПЕРВЫМ)",
        "item": [
        {
        "name": "1.1 Создать администратора (первый шаг!)",
        "request": {
        "method": "POST",
        "header": [
        {
        "key": "Content-Type",
        "value": "application/json"
        }
        ],
        "body": {
        "mode": "raw",
        "raw": "{\n  \"name\": \"Главный Администратор\",\n  \"email\": \"admin@support.com\",\n  \"password\": \"Admin@1234\"\n}"
        },
        "url": {
        "raw": "http://localhost:8080/api/auth/setup-admin",
        "protocol": "http",
        "host": ["localhost"],
        "port": "8080",
        "path": ["api", "auth", "setup-admin"]
        },
        "description": "Создает первого администратора. Выполнить один раз!"
        },
        "response": []
        },
        {
        "name": "1.2 Получить CSRF токен для сессии",
        "request": {
        "method": "GET",
        "header": [
        {
        "key": "Authorization",
        "value": "Basic YWRtaW5Ac3VwcG9ydC5jb206QWRtaW5AMTIzNA==",
        "type": "text"
        }
        ],
        "body": {
        "mode": "raw",
        "raw": ""
        },
        "url": {
        "raw": "http://localhost:8080/api/csrf/get-token",
        "protocol": "http",
        "host": ["localhost"],
        "port": "8080",
        "path": ["api", "csrf", "get-token"]
        },
        "description": "Получить CSRF токен. Токен будет работать 24 часа."
        },
        "response": []
        }
        ]
        },
        {
        "name": "2. ТЕСТ БАЗОВОГО ДОСТУПА (без CSRF)",
        "item": [
        {
        "name": "2.1 Проверить доступ (без CSRF)",
        "request": {
        "method": "GET",
        "header": [
        {
        "key": "Authorization",
        "value": "Basic YWRtaW5Ac3VwcG9ydC5jb206QWRtaW5AMTIzNA==",
        "type": "text"
        }
        ],
        "url": {
        "raw": "http://localhost:8080/api/debug/auth",
        "protocol": "http",
        "host": ["localhost"],
        "port": "8080",
        "path": ["api", "debug", "auth"]
        },
        "description": "Проверить что Basic Auth работает"
        },
        "response": []
        },
        {
        "name": "2.2 Создать тикет БЕЗ CSRF (тест)",
        "request": {
        "method": "POST",
        "header": [
        {
        "key": "Content-Type",
        "value": "application/json"
        },
        {
        "key": "Authorization",
        "value": "Basic YWRtaW5Ac3VwcG9ydC5jb206QWRtaW5AMTIzNA==",
        "type": "text"
        }
        ],
        "body": {
        "mode": "raw",
        "raw": "{\n  \"title\": \"Тестовый тикет без CSRF\",\n  \"description\": \"Проверка базового доступа\",\n  \"userId\": 1\n}"
        },
        "url": {
        "raw": "http://localhost:8080/tickets",
        "protocol": "http",
        "host": ["localhost"],
        "port": "8080",
        "path": ["tickets"]
        },
        "description": "Если этот запрос не работает (403), значит проблема не в CSRF"
        },
        "response": []
        }
        ]
        },
        {
        "name": "3. ТЕСТ С CSRF",
        "item": [
        {
        "name": "3.1 Проверить информацию о CSRF",
        "request": {
        "method": "GET",
        "header": [],
        "url": {
        "raw": "http://localhost:8080/api/csrf/info",
        "protocol": "http",
        "host": ["localhost"],
        "port": "8080",
        "path": ["api", "csrf", "info"]
        },
        "description": "Информация о настройках CSRF"
        },
        "response": []
        },
        {
        "name": "3.2 Создать тикет С CSRF (основной тест)",
        "request": {
        "method": "POST",
        "header": [
        {
        "key": "Content-Type",
        "value": "application/json"
        },
        {
        "key": "Authorization",
        "value": "Basic YWRtaW5Ac3VwcG9ydC5jb206QWRtaW5AMTIzNA==",
        "type": "text"
        },
        {
        "key": "X-XSRF-TOKEN",
        "value": "{{csrf_token}}",
        "type": "text",
        "disabled": true
        }
        ],
        "body": {
        "mode": "raw",
        "raw": "{\n  \"title\": \"Тестовый тикет с CSRF\",\n  \"description\": \"Проверка CSRF защиты\",\n  \"userId\": 1\n}"
        },
        "url": {
        "raw": "http://localhost:8080/tickets",
        "protocol": "http",
        "host": ["localhost"],
        "port": "8080",
        "path": ["tickets"]
        },
        "description": "Требует CSRF токен в заголовке X-XSRF-TOKEN"
        },
        "response": []
        }
        ]
        },
        {
        "name": "4. ДОПОЛНИТЕЛЬНЫЕ ТЕСТЫ",
        "item": [
        {
        "name": "4.1 Получить всех пользователей",
        "request": {
        "method": "GET",
        "header": [
        {
        "key": "Authorization",
        "value": "Basic YWRtaW5Ac3VwcG9ydC5jb206QWRtaW5AMTIzNA==",
        "type": "text"
        }
        ],
        "url": {
        "raw": "http://localhost:8080/users",
        "protocol": "http",
        "host": ["localhost"],
        "port": "8080",
        "path": ["users"]
        },
        "description": "Только для администраторов"
        },
        "response": []
        },
        {
        "name": "4.2 Получить все тикеты",
        "request": {
        "method": "GET",
        "header": [
        {
        "key": "Authorization",
        "value": "Basic YWRtaW5Ac3VwcG9ydC5jb206QWRtaW5AMTIzNA==",
        "type": "text"
        }
        ],
        "url": {
        "raw": "http://localhost:8080/tickets",
        "protocol": "http",
        "host": ["localhost"],
        "port": "8080",
        "path": ["tickets"]
        }
        },
        "response": []
        }
        ]
        },
        {
        "name": "5. УПРАВЛЕНИЕ РЕСУРСАМИ",
        "item": [
        {
        "name": "5.1 Создать агента",
        "request": {
        "method": "POST",
        "header": [
        {
        "key": "Content-Type",
        "value": "application/json"
        },
        {
        "key": "Authorization",
        "value": "Basic YWRtaW5Ac3VwcG9ydC5jb206QWRtaW5AMTIzNA==",
        "type": "text"
        }
        ],
        "body": {
        "mode": "raw",
        "raw": "{\n  \"name\": \"Иван Агентов\"\n}"
        },
        "url": {
        "raw": "http://localhost:8080/agents",
        "protocol": "http",
        "host": ["localhost"],
        "port": "8080",
        "path": ["agents"]
        }
        },
        "response": []
        },
        {
        "name": "5.2 Создать SLA",
        "request": {
        "method": "POST",
        "header": [
        {
        "key": "Content-Type",
        "value": "application/json"
        },
        {
        "key": "Authorization",
        "value": "Basic YWRtaW5Ac3VwcG9ydC5jb206QWRtaW5AMTIzNA==",
        "type": "text"
        }
        ],
        "body": {
        "mode": "raw",
        "raw": "{\n  \"reactionDeadlineHours\": 24,\n  \"resolutionDeadlineHours\": 72\n}"
        },
        "url": {
        "raw": "http://localhost:8080/slas",
        "protocol": "http",
        "host": ["localhost"],
        "port": "8080",
        "path": ["slas"]
        }
        },
        "response": []
        }
        ]
        }
        ],
        "event": [
        {
        "listen": "prerequest",
        "script": {
        "type": "text/javascript",
        "exec": [
        "console.log('=== Подготовка запроса ===');",
        "console.log('Метод: ' + pm.request.method);",
        "console.log('URL: ' + pm.request.url);",
        "",
        "// Проверяем, нужен ли CSRF токен",
        "const method = pm.request.method;",
        "if (method === 'POST' || method === 'PUT' || method === 'DELETE' || method === 'PATCH') {",
        "    console.log('Это модифицирующий запрос - может потребоваться CSRF');",
        "    ",
        "    // Проверяем есть ли заголовок CSRF",
        "    const csrfHeader = pm.request.headers.get('X-XSRF-TOKEN');",
        "    if (csrfHeader) {",
        "        console.log('CSRF заголовок найден: ' + csrfHeader.substring(0, 20) + '...');",
        "    } else {",
        "        console.log('CSRF заголовок не найден');",
        "    }",
        "}"
        ]
        }
        },
        {
        "listen": "test",
        "script": {
        "type": "text/javascript",
        "exec": [
        "console.log('=== Анализ ответа ===');",
        "console.log('Статус: ' + pm.response.code + ' ' + pm.response.status);",
        "console.log('Время: ' + pm.response.responseTime + 'мс');",
        "",
        "// Автоматически сохраняем CSRF токен если получен",
        "if (pm.response.code === 200 && pm.request.url.toString().includes('/api/csrf/get-token')) {",
        "    try {",
        "        const responseData = pm.response.json();",
        "        if (responseData.csrfToken) {",
        "            // Сохраняем в переменные коллекции",
        "            pm.collectionVariables.set('csrf_token', responseData.csrfToken);",
        "            console.log('✅ CSRF токен сохранен в переменные коллекции');",
        "            console.log('Токен (первые 20 символов): ' + responseData.csrfToken.substring(0, 20) + '...');",
        "        }",
        "    } catch (e) {",
        "        console.log('Не удалось сохранить CSRF токен');",
        "    }",
        "}",
        "",
        "// Анализ ошибок",
        "if (pm.response.code === 403) {",
        "    console.error('❌ Ошибка 403 - Доступ запрещен');",
        "    console.error('Возможные причины:');",
        "    console.error('1. Нет CSRF токена для POST/PUT/DELETE запросов');",
        "    console.error('2. Недостаточно прав (не та роль пользователя)');",
        "    console.error('3. CSRF токен неверный или устарел');",
        "}",
        "",
        "if (pm.response.code === 401) {",
        "    console.error('❌ Ошибка 401 - Не авторизован');",
        "    console.error('Проверьте логин и пароль в заголовке Authorization');",
        "}",
        "",
        "// Базовые тесты",
        "pm.test('Статус код получен', function () {",
        "    pm.response.to.have.status;",
        "});",
        "",
        "pm.test('Ответ пришел', function () {",
        "    pm.expect(pm.response.responseTime).to.be.below(10000);",
        "});"
        ]
        }
        }
        ],
        "variable": [
        {
        "key": "csrf_token",
        "value": "",
        "type": "string"
        },
        {
        "key": "base_url",
        "value": "http://localhost:8080",
        "type": "string"
        }
        ]
        }