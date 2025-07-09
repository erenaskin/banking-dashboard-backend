# Banking Dashboard Backend

Banking Dashboard, modern teknolojilerle geliştirilmiş, güvenli, kurumsal seviyede bir bankacılık web uygulamasıdır. Proje, Java Spring Boot tabanlı backend mimarisi ve RESTful API yapısı ile gerçek dünya finansal işlemlerini simüle eder.

---

## İçindekiler

- [Genel Bakış](#genel-bakış)  
- [Özellikler](#özellikler)  
- [Teknolojiler](#teknolojiler)  
- [Proje Mimarisi](#proje-mimarisi)  
- [Kurulum ve Çalıştırma](#kurulum-ve-çalıştırma)  
- [API Dökümantasyonu](#api-dökümantasyonu)  
- [Testler](#testler)  
- [Güvenlik](#güvenlik)  
- [Geliştirme ve Katkı](#geliştirme-ve-katkı)  
- [Lisans](#lisans)

---

## Genel Bakış

Bu proje, banka müşterilerinin hesap oluşturma, para transferi, işlem geçmişini görüntüleme ve daha birçok bankacılık işlemini güvenli bir şekilde yapmasını sağlar. 

Backend katmanı, **Spring Boot** kullanılarak geliştirilmiş olup, JWT tabanlı güvenlik, veri doğrulama, hata yönetimi ve test otomasyonlarını kapsamaktadır.

---

## Özellikler

- **Kullanıcı Kayıt & Giriş (JWT Authentication)**
- **Hesap Yönetimi:** Hesap oluşturma, hesap detaylarını görüntüleme, kullanıcıya ait hesapların listelenmesi.
- **Para Transferi:** Hesaplar arası para gönderme, para yatırma (deposit), çekme (withdrawal) işlemleri.
- **İşlem Geçmişi:** Hesap bazında tüm para hareketlerini listeleme.
- **Kurumsal Düzeyde Güvenlik:** JWT ile yetkilendirme, parola şifreleme.
- **DTO ve MapStruct ile Katmanlı Tasarım:** Veri transfer objeleri ve haritalama işlemleri.
- **Global Exception Handling:** Merkezi hata yakalama ve tutarlı API hata cevapları.
- **Entegrasyon Testleri:** Controller ve service katmanları için kapsamlı uçtan uca testler.
- **Unit Testler:** Servis katmanı için detaylı birim testler.
- **OpenAPI/Swagger (isteğe bağlı):** API dökümantasyonu.

---

## Teknolojiler

- Java 17
- Spring Boot 3.5.x
- Spring Security (JWT)
- Spring Data JPA
- PostgreSQL
- Maven
- MapStruct
- JUnit 5 & Mockito
- Spring Test (MockMvc)
- Jackson (JSON işlemleri)
- Lombok
- Testcontainers (Opsiyonel, integration test için)

---

## Proje Mimarisi

Projede klasik **3 katmanlı mimari** benimsenmiştir:

- **Controller:** API endpoint’leri, HTTP isteklerini alır, servisleri çağırır.
- **Service:** İş mantığı burada yer alır, işlemler bu katmanda yönetilir.
- **Repository:** Veritabanı işlemleri JPA repository ile gerçekleştirilir.

**DTO'lar (Data Transfer Objects)** ile API ve entity katmanı arasında veri geçişi soyutlanmıştır. MapStruct kullanılarak veri dönüşümleri otomatik hale getirilmiştir.

---

## Kurulum ve Çalıştırma

1. **Projeyi Klonlayın:**

   ```bash
   git clone https://github.com/erenaskin/banking-dashboard.git
   cd banking-dashboard
````

2. **PostgreSQL Veritabanını Ayarlayın:**

   * PostgreSQL 17 veya uyumlu bir sürümü kurun.
   * Veritabanı ve kullanıcı oluşturun.
   * `application.properties` veya `application.yml` içinde veritabanı bağlantı bilgilerinizi güncelleyin.

3. **Projeyi Build Edin ve Çalıştırın:**

   ```bash
   ./mvnw clean spring-boot:run
   ```

4. **Testleri Çalıştırın:**

   ```bash
   ./mvnw test
   ```

---

## API Dökümantasyonu

* Tüm API endpoint’leri REST standartlarına uygundur.
* JWT Token ile yetkilendirme gerektirir.
* Başlıca endpointler:

| Method | URL                               | Açıklama                           |
| ------ | --------------------------------- | ---------------------------------- |
| POST   | /api/auth/register                | Kullanıcı kaydı                    |
| POST   | /api/auth/login                   | Kullanıcı girişi (token al)        |
| POST   | /api/accounts                     | Hesap oluşturma                    |
| GET    | /api/accounts                     | Kullanıcının hesaplarını listeleme |
| GET    | /api/accounts/{iban}/currency     | Hesap para birimini alma           |
| GET    | /api/accounts/{iban}/details      | Hesap detaylarını alma             |
| POST   | /api/accounts/{iban}/transactions | Para transferi, işlem yapma        |
| POST   | /api/transactions                 | Transfer işlemi                    |
| GET    | /api/transactions/{iban}          | Hesap işlem geçmişi                |

---

## Testler

Projede kapsamlı testler bulunmaktadır:

* **Unit Testler:** Servis katmanındaki iş mantığını test eder.
* **Integration Testler:** Controller ve servisleri uçtan uca test ederek API endpointlerinin doğru çalışmasını sağlar.
* Testler için MockMvc, JUnit 5, Mockito ve Spring Boot Test framework’ü kullanılmıştır.
* Hataların erken tespiti için testler CI pipeline'ına entegre edilmesi önerilir.

---

## Güvenlik

* Kullanıcı kimlik doğrulaması JWT (JSON Web Token) ile yapılır.
* Parolalar BCrypt ile şifrelenir.
* Tüm API endpointleri yetkilendirme mekanizması ile korunur.
* Global Exception Handler ile tutarlı ve anlamlı hata mesajları sağlanır.

---

## Geliştirme ve Katkı

* Kod standartları ve temiz kod prensiplerine dikkat edilmiştir.
* Yeni özellik eklemek veya hataları düzeltmek için pull request açabilirsiniz.
* Proje modüler yapıda olduğundan geliştirme kolaydır.
* MapStruct ve DTO kullanımıyla entity ve API yapısı ayrı tutulmuştur.

---

## İletişim

Geliştirici: **Eren AŞKIN**
GitHub: [https://github.com/erenaskin](https://github.com/erenaskin)
Email: [eren.askin@hotmail.com](mailto:eren.askin@hotmail.com)


