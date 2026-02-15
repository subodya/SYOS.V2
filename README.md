
```
SYOS.V2
├─ pom.xml
├─ syos-client
│  ├─ pom.xml
│  ├─ src
│  │  └─ main
│  │     ├─ java
│  │     │  └─ com
│  │     │     └─ syos
│  │     │        └─ client
│  │     │           ├─ ClientApplication.java
│  │     │           ├─ concurrency
│  │     │           │  └─ AsyncTaskExecutor.java
│  │     │           ├─ presentation
│  │     │           │  └─ controllers
│  │     │           │     ├─ CheckoutController.java
│  │     │           │     ├─ DashboardController.java
│  │     │           │     ├─ InventoryController.java
│  │     │           │     ├─ MainController.java
│  │     │           │     ├─ POSCheckoutController.java
│  │     │           │     └─ ReportsController.java
│  │     │           └─ services
│  │     │              └─ ServerConnection.java
│  │     └─ resources
│  │        ├─ css
│  │        │  └─ modern-styles.css
│  │        ├─ fxml
│  │        │  ├─ Dashboard.fxml
│  │        │  ├─ Inventory.fxml
│  │        │  ├─ Main.fxml
│  │        │  ├─ POSCheckout.fxml
│  │        │  └─ Reports.fxml
│  │        └─ views
│  │           └─ CheckoutView.fxml
│  └─ target
│     ├─ classes
│     │  ├─ com
│     │  │  └─ syos
│     │  │     └─ client
│     │  │        ├─ ClientApplication.class
│     │  │        ├─ concurrency
│     │  │        │  └─ AsyncTaskExecutor.class
│     │  │        ├─ presentation
│     │  │        │  └─ controllers
│     │  │        │     ├─ CheckoutController$CartItemView.class
│     │  │        │     ├─ CheckoutController.class
│     │  │        │     ├─ DashboardController.class
│     │  │        │     ├─ InventoryController$1.class
│     │  │        │     ├─ InventoryController$2.class
│     │  │        │     ├─ InventoryController.class
│     │  │        │     ├─ MainController.class
│     │  │        │     ├─ POSCheckoutController$CartItem.class
│     │  │        │     ├─ POSCheckoutController.class
│     │  │        │     └─ ReportsController.class
│     │  │        └─ services
│     │  │           ├─ ServerConnection$1.class
│     │  │           ├─ ServerConnection$2.class
│     │  │           └─ ServerConnection.class
│     │  ├─ css
│     │  │  └─ modern-styles.css
│     │  ├─ fxml
│     │  │  ├─ Dashboard.fxml
│     │  │  ├─ Inventory.fxml
│     │  │  ├─ Main.fxml
│     │  │  ├─ POSCheckout.fxml
│     │  │  └─ Reports.fxml
│     │  └─ views
│     │     └─ CheckoutView.fxml
│     ├─ generated-sources
│     │  └─ annotations
│     └─ maven-status
│        └─ maven-compiler-plugin
│           └─ compile
│              └─ default-compile
│                 ├─ createdFiles.lst
│                 └─ inputFiles.lst
├─ syos-common
│  ├─ pom.xml
│  ├─ src
│  │  └─ main
│  │     └─ java
│  │        └─ com
│  │           └─ syos
│  │              └─ common
│  │                 ├─ dto
│  │                 │  ├─ BillDto.java
│  │                 │  ├─ BillItemDto.java
│  │                 │  ├─ CheckoutRequest.java
│  │                 │  ├─ CustomerDto.java
│  │                 │  ├─ InventoryBatchDto.java
│  │                 │  └─ ItemDto.java
│  │                 └─ util
│  │                    └─ JsonUtil.java
│  └─ target
│     ├─ classes
│     │  └─ com
│     │     └─ syos
│     │        └─ common
│     │           ├─ dto
│     │           │  ├─ BillDto.class
│     │           │  ├─ BillItemDto.class
│     │           │  ├─ CheckoutRequest.class
│     │           │  ├─ CustomerDto.class
│     │           │  ├─ InventoryBatchDto.class
│     │           │  └─ ItemDto.class
│     │           └─ util
│     │              └─ JsonUtil.class
│     ├─ generated-sources
│     │  └─ annotations
│     ├─ maven-archiver
│     │  └─ pom.properties
│     ├─ maven-status
│     │  └─ maven-compiler-plugin
│     │     └─ compile
│     │        └─ default-compile
│     │           ├─ createdFiles.lst
│     │           └─ inputFiles.lst
│     └─ syos-common-2.0.0.jar
├─ syos-database
│  ├─ dependency-reduced-pom.xml
│  ├─ pom.xml
│  ├─ src
│  │  └─ main
│  │     └─ java
│  │        └─ com
│  │           └─ syos
│  │              └─ database
│  │                 └─ DatabaseServer.java
│  └─ target
│     ├─ classes
│     │  └─ com
│     │     └─ syos
│     │        └─ database
│     │           └─ DatabaseServer.class
│     ├─ generated-sources
│     │  └─ annotations
│     ├─ maven-archiver
│     │  └─ pom.properties
│     ├─ maven-status
│     │  └─ maven-compiler-plugin
│     │     └─ compile
│     │        └─ default-compile
│     │           ├─ createdFiles.lst
│     │           └─ inputFiles.lst
│     ├─ original-syos-database-2.0.0.jar
│     └─ syos-database-2.0.0.jar
├─ syos-server
│  ├─ dependency-reduced-pom.xml
│  ├─ pom.xml
│  ├─ src
│  │  └─ main
│  │     ├─ java
│  │     │  └─ com
│  │     │     └─ syos
│  │     │        └─ server
│  │     │           ├─ business
│  │     │           │  ├─ BusinessFacade.java
│  │     │           │  ├─ services
│  │     │           │  └─ usecases
│  │     │           │     ├─ CheckoutUseCase.java
│  │     │           │     └─ InventoryManagementUseCase.java
│  │     │           ├─ concurrency
│  │     │           │  ├─ ClientRequest.java
│  │     │           │  ├─ RequestProcessor.java
│  │     │           │  └─ ServerMetrics.java
│  │     │           ├─ domain
│  │     │           │  ├─ entities
│  │     │           │  │  ├─ Bill.java
│  │     │           │  │  ├─ BillItem.java
│  │     │           │  │  ├─ Customer.java
│  │     │           │  │  └─ Item.java
│  │     │           │  ├─ repositories
│  │     │           │  │  ├─ IBillRepository.java
│  │     │           │  │  ├─ ICustomerRepository.java
│  │     │           │  │  └─ IItemRepository.java
│  │     │           │  └─ valueobjects
│  │     │           ├─ infrastructure
│  │     │           │  ├─ database
│  │     │           │  │  └─ DatabaseConnection.java
│  │     │           │  └─ repositories
│  │     │           │     ├─ BillRepositoryImpl.java
│  │     │           │     ├─ CustomerRepositoryImpl.java
│  │     │           │     └─ ItemRepositoryImpl.java
│  │     │           ├─ presentation
│  │     │           │  ├─ servlets
│  │     │           │  │  ├─ CheckoutServlet.java
│  │     │           │  │  ├─ InventoryServlet.java
│  │     │           │  │  ├─ ItemServlet.java
│  │     │           │  │  └─ ServerStatusServlet.java
│  │     │           │  └─ websocket
│  │     │           └─ ServerApplication.java
│  │     ├─ resources
│  │     │  └─ database
│  │     │     └─ schema.sql
│  │     └─ webapp
│  │        └─ WEB-INF
│  └─ target
│     ├─ classes
│     │  ├─ com
│     │  │  └─ syos
│     │  │     └─ server
│     │  │        ├─ business
│     │  │        │  ├─ BusinessFacade.class
│     │  │        │  └─ usecases
│     │  │        │     ├─ CheckoutUseCase.class
│     │  │        │     └─ InventoryManagementUseCase.class
│     │  │        ├─ concurrency
│     │  │        │  ├─ ClientRequest$RequestType.class
│     │  │        │  ├─ ClientRequest.class
│     │  │        │  ├─ RequestProcessor$1.class
│     │  │        │  ├─ RequestProcessor$WorkerThread.class
│     │  │        │  ├─ RequestProcessor.class
│     │  │        │  └─ ServerMetrics.class
│     │  │        ├─ domain
│     │  │        │  ├─ entities
│     │  │        │  │  ├─ Bill.class
│     │  │        │  │  ├─ BillItem.class
│     │  │        │  │  ├─ Customer.class
│     │  │        │  │  └─ Item.class
│     │  │        │  └─ repositories
│     │  │        │     ├─ IBillRepository.class
│     │  │        │     ├─ ICustomerRepository.class
│     │  │        │     └─ IItemRepository.class
│     │  │        ├─ infrastructure
│     │  │        │  ├─ database
│     │  │        │  │  └─ DatabaseConnection.class
│     │  │        │  └─ repositories
│     │  │        │     ├─ BillRepositoryImpl.class
│     │  │        │     ├─ CustomerRepositoryImpl.class
│     │  │        │     └─ ItemRepositoryImpl.class
│     │  │        ├─ presentation
│     │  │        │  └─ servlets
│     │  │        │     ├─ CheckoutServlet.class
│     │  │        │     ├─ InventoryServlet.class
│     │  │        │     ├─ ItemServlet.class
│     │  │        │     └─ ServerStatusServlet.class
│     │  │        └─ ServerApplication.class
│     │  └─ database
│     │     └─ schema.sql
│     ├─ generated-sources
│     │  └─ annotations
│     ├─ maven-archiver
│     │  └─ pom.properties
│     ├─ maven-status
│     │  └─ maven-compiler-plugin
│     │     └─ compile
│     │        └─ default-compile
│     │           ├─ createdFiles.lst
│     │           └─ inputFiles.lst
│     ├─ original-syos-server-2.0.0.jar
│     └─ syos-server-2.0.0.jar
└─ syos-test-clients
   ├─ pom.xml
   ├─ src
   │  └─ main
   │     └─ java
   │        └─ com
   │           └─ syos
   │              └─ test
   │                 ├─ ConcurrentLoadTester.java
   │                 └─ RapidCheckoutTest.java
   └─ target
      ├─ classes
      │  └─ com
      │     └─ syos
      │        └─ test
      │           ├─ ConcurrentLoadTester.class
      │           ├─ RapidCheckoutTest$HttpWorker.class
      │           └─ RapidCheckoutTest.class
      ├─ generated-sources
      │  └─ annotations
      ├─ maven-archiver
      │  └─ pom.properties
      ├─ maven-status
      │  └─ maven-compiler-plugin
      │     └─ compile
      │        └─ default-compile
      │           ├─ createdFiles.lst
      │           └─ inputFiles.lst
      └─ syos-test-clients-2.0.0.jar

```