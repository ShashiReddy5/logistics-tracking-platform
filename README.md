# logistics-tracking-platform
Real-time shipment tracking with Kafka streaming, Oracle PL/SQL, Elasticsearch analytics, and Angular dashboards
# Logistics Tracking Platform

[![Java](https://img.shields.io/badge/Java-11-ED8B00?style=flat&logo=java)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7-6DB33F?style=flat&logo=spring-boot)](https://spring.io/projects/spring-boot)
[![Kafka](https://img.shields.io/badge/Apache%20Kafka-3.x-231F20?style=flat&logo=apache-kafka)](https://kafka.apache.org)
[![Elasticsearch](https://img.shields.io/badge/Elasticsearch-8.x-005571?style=flat&logo=elasticsearch)](https://www.elastic.co)
[![Angular](https://img.shields.io/badge/Angular-14-DD0031?style=flat&logo=angular)](https://angular.io)

Real-time shipment tracking backend with Kafka streaming pipelines, Oracle PL/SQL data synchronisation, Elasticsearch analytics, and Angular/React dashboards. Processes millions of shipment events daily across global logistics networks.

Inspired by enterprise logistics platform architecture built at FedEx.

---

## Architecture Overview

```
┌──────────────────────────────────────────────────────────────────┐
│                     Angular / React Dashboard                     │
│              (SLA monitoring · route visibility · alerts)         │
└────────────────────────────┬─────────────────────────────────────┘
                             │ REST API
              ┌──────────────▼──────────────┐
              │   Shipment Tracking Service  │
              │   (Spring Boot / Java 11)    │
              └──┬──────────────────────┬───┘
                 │                      │
    ┌────────────▼──────┐   ┌───────────▼──────────┐
    │  Oracle DB (RDS)  │   │  Kafka Cluster        │
    │  PL/SQL pipelines │   │  shipment-events      │
    └───────────────────┘   │  delivery-updates     │
                            └───────────┬──────────┘
                                        │
                    ┌───────────────────▼────────────────┐
                    │       Elasticsearch + Kibana         │
                    │  (analytics · KPIs · SLA dashboards) │
                    └────────────────────────────────────┘
```

---

## Key Features

- **Kafka streaming** — consumes millions of shipment events/day (induction, sorting, routing, delivery confirmation) with consumer group partitioning for parallel processing
- **Oracle PL/SQL pipelines** — synchronises shipment data across tracking, billing, and warehouse platforms via complex stored procedures
- **Elasticsearch analytics** — on-time delivery rates, exception volumes, carrier performance KPIs in real time
- **Kibana dashboards** — operational intelligence for logistics managers across global regions
- **Angular SPA** — real-time route visibility, SLA monitoring, and exception management screens fed by live Kafka data
- **JWT/OAuth2 security** — Spring Security-secured REST APIs with role-based access for internal vs partner portals

---

## Tech Stack

| Component | Technology |
|---|---|
| Language | Java 11 |
| Framework | Spring Boot 2.7, Spring MVC, Spring Security, Spring Batch |
| Persistence | Spring Data JPA, Hibernate, Oracle SQL/PL-SQL |
| Messaging | Apache Kafka 3.x |
| Search / Analytics | Elasticsearch 8.x, Kibana |
| Frontend | Angular 14, TypeScript, RxJS |
| Cloud | AWS (EC2, ECS, RDS, S3, Lambda, SQS, CloudWatch) |
| Container | Docker, Kubernetes |
| CI/CD | Jenkins, GitLab CI/CD |
| Security | JWT, OAuth2, Spring Security |

---

## Core Code Samples

### Kafka Shipment Event Consumer

```java
@Component
@Slf4j
public class ShipmentEventConsumer {

    private final ShipmentTrackingService trackingService;
    private final ElasticsearchIndexer esIndexer;

    @KafkaListener(
        topics = "${kafka.topics.shipment-events}",
        groupId = "${kafka.consumer.group-id}",
        containerFactory = "shipmentKafkaListenerContainerFactory"
    )
    public void consumeShipmentEvent(
            @Payload ShipmentEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Processing shipment event: trackingId={}, status={}, partition={}, offset={}",
            event.getTrackingId(), event.getStatus(), partition, offset);

        try {
            trackingService.updateShipmentStatus(event);
            esIndexer.indexShipmentEvent(event); // for Kibana analytics
        } catch (Exception e) {
            log.error("Failed to process shipment event: trackingId={}", event.getTrackingId(), e);
            throw e; // triggers retry / DLQ routing
        }
    }
}
```

### Oracle PL/SQL Stored Procedure (Java call)

```java
@Repository
public class ShipmentSyncRepository {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Calls Oracle stored procedure to sync shipment data
     * across tracking, billing, and warehouse platforms.
     * Encapsulates complex multi-table reconciliation logic.
     */
    @Transactional
    public void syncShipmentToBilling(String trackingId, LocalDate billingDate) {
        StoredProcedureQuery query = entityManager
            .createStoredProcedureQuery("PKG_SHIPMENT_SYNC.SYNC_TO_BILLING")
            .registerStoredProcedureParameter("p_tracking_id", String.class, ParameterMode.IN)
            .registerStoredProcedureParameter("p_billing_date", Date.class, ParameterMode.IN)
            .registerStoredProcedureParameter("p_result_code", Integer.class, ParameterMode.OUT)
            .registerStoredProcedureParameter("p_error_msg", String.class, ParameterMode.OUT)
            .setParameter("p_tracking_id", trackingId)
            .setParameter("p_billing_date", Date.valueOf(billingDate));

        query.execute();

        Integer resultCode = (Integer) query.getOutputParameterValue("p_result_code");
        if (resultCode != 0) {
            String errorMsg = (String) query.getOutputParameterValue("p_error_msg");
            throw new ShipmentSyncException("Billing sync failed: " + errorMsg);
        }
    }
}
```

### Elasticsearch Indexer for Shipment Analytics

```java
@Component
@Slf4j
public class ElasticsearchIndexer {

    private final ElasticsearchOperations esOperations;

    public void indexShipmentEvent(ShipmentEvent event) {
        ShipmentDocument doc = ShipmentDocument.builder()
            .trackingId(event.getTrackingId())
            .carrier(event.getCarrier())
            .status(event.getStatus().name())
            .originHub(event.getOriginHub())
            .destinationHub(event.getDestinationHub())
            .eventTimestamp(event.getEventTimestamp())
            .estimatedDelivery(event.getEstimatedDelivery())
            .onTime(calculateOnTimeStatus(event))
            .build();

        esOperations.save(doc);
        log.debug("Indexed shipment event: trackingId={}", event.getTrackingId());
    }

    private boolean calculateOnTimeStatus(ShipmentEvent event) {
        if (event.getStatus() == ShipmentStatus.DELIVERED && event.getActualDelivery() != null) {
            return !event.getActualDelivery().isAfter(event.getEstimatedDelivery());
        }
        return true; // in-transit — not yet late
    }
}
```

### Angular Real-Time SLA Dashboard (TypeScript)

```typescript
@Component({
  selector: 'app-sla-dashboard',
  template: `
    <div class="dashboard-grid">
      <mat-card *ngFor="let metric of slaMetrics$ | async">
        <mat-card-title>{{ metric.carrier }}</mat-card-title>
        <mat-card-content>
          <div class="on-time-rate" [class.warning]="metric.onTimeRate < 0.95">
            On-Time: {{ metric.onTimeRate | percent:'1.1-1' }}
          </div>
          <div class="exception-count">
            Exceptions: {{ metric.exceptionCount | number }}
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `
})
export class SlaDashboardComponent implements OnInit, OnDestroy {

  slaMetrics$: Observable<SlaMetric[]>;
  private refreshInterval$ = interval(30000); // refresh every 30s
  private destroy$ = new Subject<void>();

  constructor(private shipmentService: ShipmentService) {}

  ngOnInit(): void {
    this.slaMetrics$ = merge(of(null), this.refreshInterval$).pipe(
      switchMap(() => this.shipmentService.getSlaMetrics()),
      catchError(err => {
        console.error('Failed to load SLA metrics', err);
        return EMPTY;
      }),
      takeUntil(this.destroy$)
    );
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
```

---

## Running Locally

```bash
# start dependencies
docker-compose up -d oracle kafka elasticsearch kibana

# run the service
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# run tests
./mvnw test

# access Kibana dashboards
open http://localhost:5601
```

