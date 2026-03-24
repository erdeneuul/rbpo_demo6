# РБПО 2025 — Задание 3
## Тема: Аренда недвижимости (Вариант 13)

## Стек
- Java 21
- Spring Boot 3.5.5
- Spring Data JPA
- PostgreSQL

## Как запустить

1. Установить PostgreSQL
2. Создать базу данных: `CREATE DATABASE rental_db;`
3. Установить переменные окружения (или изменить application.properties):
   - `DB_URL` = `jdbc:postgresql://localhost:5432/rental_db`
   - `DB_USERNAME` = `postgres`
   - `DB_PASSWORD` = ваш_пароль
4. Запустить: `mvn spring-boot:run`

Таблицы создадутся автоматически при первом запуске.

---

## Сущности и связи

- **Landlord** (арендодатель) → владеет многими **Property**
- **Property** (объект) → принадлежит одному **Landlord**, имеет много **Lease**
- **Tenant** (арендатор) → имеет много **Lease**
- **Lease** (договор) → связывает **Property** и **Tenant**, имеет много **Payment**
- **Payment** (платёж) → принадлежит одному **Lease**

---

## Бизнес-операции

| URL | Описание |
|-----|----------|
| `POST /leases/create?propertyId=1&tenantId=1&startDate=2025-07-01&endDate=2025-12-31` | Создать договор с проверкой пересечений |
| `POST /leases/{id}/cancel` | Отменить договор + пометить платежи как OVERDUE |
| `POST /leases/{id}/complete` | Завершить договор |
| `POST /leases/{id}/pay-all` | Оплатить все pending/overdue платежи по договору |
| `GET /leases/{id}/info` | Полная информация по договору (объект + арендатор + платежи) |
