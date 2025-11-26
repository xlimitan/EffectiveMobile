package com.example.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/docs")
public class DocumentationController {

    @GetMapping
    public ResponseEntity<String> getApiDocs() {
        return ResponseEntity.ok("""
            <!DOCTYPE html>
            <html>
            <head>
                <title>🏦 Банковское API</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 40px; }
                    h1 { color: #2c3e50; }
                    h2 { color: #34495e; border-bottom: 2px solid #3498db; }
                    h3 { color: #2980b9; }
                    code { background: #f8f9fa; padding: 2px 6px; border-radius: 3px; }
                    pre { background: #f8f9fa; padding: 15px; border-radius: 5px; overflow-x: auto; }
                    .endpoint { background: #e8f4fd; padding: 10px; margin: 10px 0; border-left: 4px solid #3498db; }
                </style>
            </head>
            <body>
                <h1>🏦 Банковское API</h1>
                
                <h2>🔐 Аутентификация</h2>
                
                <div class="endpoint">
                    <h3>POST /api/auth/login</h3>
                    <p>Авторизация пользователя</p>
                    <pre><code>{
  "username": "admin1",
  "password": "admin123"
}</code></pre>
                </div>
                
                <div class="endpoint">
                    <h3>POST /api/auth/register</h3>
                    <p>Регистрация пользователя</p>
                    <pre><code>{
  "username": "newuser",
  "password": "password123",
  "name": "Иван",
  "surname": "Иванов",
  "patronymic": "Иванович",
  "birthdayYear": 1990
}</code></pre>
                </div>
                
                <h2>👤 Пользователи (ADMIN)</h2>
                
                <div class="endpoint">
                    <h3>GET /api/users</h3>
                    <p>Получить всех пользователей</p>
                </div>
                
                <div class="endpoint">
                    <h3>POST /api/users</h3>
                    <p>Создать пользователя</p>
                </div>
                
                <div class="endpoint">
                    <h3>PUT /api/users/{id}</h3>
                    <p>Обновить пользователя</p>
                </div>
                
                <div class="endpoint">
                    <h3>DELETE /api/users/{id}</h3>
                    <p>Удалить пользователя</p>
                </div>
                
                <h2>💳 Карты</h2>
                
                <div class="endpoint">
                    <h3>GET /api/cards</h3>
                    <p>Получить все карты (ADMIN)</p>
                </div>
                
                <div class="endpoint">
                    <h3>POST /api/cards</h3>
                    <p>Создать карту (ADMIN)</p>
                </div>
                
                <div class="endpoint">
                    <h3>GET /api/cards/{id}</h3>
                    <p>Просмотр карты (USER)</p>
                </div>
                
                <div class="endpoint">
                    <h3>DELETE /api/cards/{id}</h3>
                    <p>Удалить карту (ADMIN)</p>
                </div>
                
                <div class="endpoint">
                    <h3>PATCH /api/cards/block/{id}</h3>
                    <p>Заблокировать карту (ADMIN)</p>
                </div>
                
                <div class="endpoint">
                    <h3>PATCH /api/cards/{id}/top-up</h3>
                    <p>Пополнить карту (USER)</p>
                </div>
                
                <h2>💰 Транзакции</h2>
                
                <div class="endpoint">
                    <h3>GET /api/transactions</h3>
                    <p>Получить все транзакции (ADMIN)</p>
                </div>
                
                <div class="endpoint">
                    <h3>POST /api/transactions/transfer</h3>
                    <p>Перевод между картами (USER)</p>
                    <pre><code>{
  "fromCardId": "uuid",
  "toCardId": "uuid",
  "amount": 100.50
}</code></pre>
                </div>
                
                <div class="endpoint">
                    <h3>GET /api/transactions/my</h3>
                    <p>Мои транзакции (USER)</p>
                </div>
                
                <div class="endpoint">
                    <h3>DELETE /api/transactions/{id}</h3>
                    <p>Удалить транзакцию (ADMIN)</p>
                </div>
                
                <h2>🔐 Авторизация</h2>
                <p>Для всех защищенных эндпоинтов добавьте заголовок:</p>
                <pre><code>Authorization: Bearer your_jwt_token</code></pre>
                <h3>Author: Ilia Ivanov</h3>
                <h4>Github link: </h4><a href="https://github.com/Luxtington">https://github.com/Luxtington</a>
            </body>
            </html>
            """);
    }
}
