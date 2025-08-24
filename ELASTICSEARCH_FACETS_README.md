# Elasticsearch Facets Implementation - Doctor Service

## 🎯 Resumen de la Solución

Se ha implementado una solución completa de **facets con Spring Data Elasticsearch** siguiendo el patrón del [repositorio de ejemplo](https://github.com/UnirCs/back-end-facets/blob/master/). Esta implementación resuelve el warning de Spring Data y proporciona capacidades avanzadas de búsqueda con agregaciones.

## 🏗️ Arquitectura de la Solución

### 1. **Separación de Responsabilidades**
- **`Doctor` (JPA)**: Entidad para persistencia en base de datos
- **`DoctorElasticsearch`**: Entidad para búsquedas en Elasticsearch
- **`DoctorRepository`**: Repositorio JPA para operaciones CRUD
- **`DoctorElasticsearchRepository`**: Repositorio Spring Data Elasticsearch para facets

### 2. **Componentes Implementados**

#### Entidad Elasticsearch (`DoctorElasticsearch.java`)
```java
@Document(indexName = "doctores")
@Setting(settingPath = "elasticsearch-settings.json")
public class DoctorElasticsearch {
    @Field(type = FieldType.Keyword)  // Para facets
    private String specialty;
    
    @Field(type = FieldType.Text, analyzer = "standard")  // Para búsqueda de texto
    private String searchText;
}
```

#### Repositorio Spring Data (`DoctorElasticsearchRepository.java`)
```java
@Repository
public interface DoctorElasticsearchRepository extends ElasticsearchRepository<DoctorElasticsearch, String> {
    // Métodos automáticos de Spring Data
    List<DoctorElasticsearch> findBySpecialty(String specialty);
    
    // Queries personalizadas con @Query
    @Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"name^3\", \"specialty^2.5\"]}}")
    Page<DoctorElasticsearch> searchByText(String query, Pageable pageable);
}
```

#### Servicio de Facets (`DoctorElasticsearchService.java`)
```java
@Service
public class DoctorElasticsearchService {
    public Map<String, Object> searchWithFacets(String query, String specialty, ...) {
        // Implementa agregaciones para facets
        addFacets(sourceBuilder);
        // Procesa resultados con facets
        return processFacets(response);
    }
}
```

## 🚀 Endpoints Disponibles

### Búsqueda con Facets
```
GET /api/elasticsearch/doctors/search-with-facets
```

**Parámetros:**
- `query`: Texto de búsqueda libre
- `specialty`: Filtro por especialidad
- `hospital`: Filtro por hospital
- `minExperience`, `maxExperience`: Rango de experiencia
- `minRating`, `maxRating`: Rango de rating
- `available`: Disponibilidad
- `tags`: Lista de tags
- `page`, `size`: Paginación

**Respuesta con Facets:**
```json
{
  "doctors": [...],
  "totalHits": 150,
  "facets": {
    "specialties": [
      {"value": "Cardiología", "count": 25},
      {"value": "Neurología", "count": 18}
    ],
    "hospitals": [
      {"value": "Hospital Central", "count": 45},
      {"value": "Clínica Norte", "count": 32}
    ],
    "avgRating": 4.2,
    "avgExperience": 12.5
  }
}
```

### Otros Endpoints
- `GET /api/elasticsearch/doctors/specialties` - Todas las especialidades
- `GET /api/elasticsearch/doctors/hospitals` - Todos los hospitales
- `GET /api/elasticsearch/doctors/tags` - Todos los tags
- `POST /api/elasticsearch/doctors/sync` - Sincronizar desde JPA

## 🔧 Configuración

### 1. **Archivo de Configuración** (`elasticsearch-settings.json`)
```json
{
  "analysis": {
    "analyzer": {
      "standard": {"type": "standard"},
      "spanish": {"type": "spanish"}
    }
  },
  "index": {
    "number_of_shards": 1,
    "number_of_replicas": 0
  }
}
```

### 2. **Anotaciones en Entidad**
- `@Document`: Define el índice de Elasticsearch
- `@Field(type = FieldType.Keyword)`: Para campos de facet (agregaciones)
- `@Field(type = FieldType.Text)`: Para búsqueda de texto completo

## 📊 Tipos de Facets Implementados

### 1. **Facets de Términos**
- **Especialidades**: Conteo de doctores por especialidad
- **Hospitales**: Conteo de doctores por hospital
- **Tags**: Conteo de doctores por tag
- **Días Laborales**: Conteo por día de trabajo

### 2. **Facets de Rango**
- **Experiencia**: Rangos 0-5, 6-10, 11-15, 16-20, 21+ años
- **Rating**: Rangos 1-2, 2-3, 3-4, 4-5 estrellas

### 3. **Facets de Métricas**
- **Rating Promedio**: Promedio de rating de todos los doctores
- **Experiencia Promedio**: Promedio de años de experiencia

## 🔄 Sincronización JPA ↔ Elasticsearch

```java
// Sincronizar un doctor desde JPA
@PostMapping("/sync")
public ResponseEntity<DoctorElasticsearch> syncDoctor(@RequestBody Doctor doctor) {
    DoctorElasticsearch doctorES = doctorElasticsearchService.syncFromJPA(doctor);
    return ResponseEntity.ok(doctorES);
}
```

## ✅ Beneficios de la Nueva Implementación

1. **Resuelve el Warning**: Spring Data ahora identifica correctamente los repositorios
2. **Facets Nativos**: Implementación nativa de Elasticsearch con agregaciones
3. **Separación Clara**: JPA para persistencia, Elasticsearch para búsqueda
4. **Performance**: Búsquedas optimizadas con índices apropiados
5. **Escalabilidad**: Fácil agregar nuevos tipos de facets

## 🧪 Testing

### Probar Búsqueda con Facets
```bash
curl "http://localhost:8081/api/elasticsearch/doctors/search-with-facets?query=cardio&page=0&size=10"
```

### Probar Facets Individuales
```bash
curl "http://localhost:8081/api/elasticsearch/doctors/specialties"
curl "http://localhost:8081/api/elasticsearch/doctors/hospitals"
```

## 🚨 Solución al Warning Original

**Antes (Problemático):**
```
Spring Data Elasticsearch - Could not safely identify store assignment for repository candidate interface com.hn.tgu.hospital.repository.DoctorRepository
```

**Después (Resuelto):**
- `DoctorRepository` extiende `JpaRepository` → Para JPA
- `DoctorElasticsearchRepository` extiende `ElasticsearchRepository` → Para Elasticsearch
- Spring Data identifica correctamente cada repositorio

## 📚 Referencias

- [Repositorio de Ejemplo](https://github.com/UnirCs/back-end-facets/blob/master/)
- [Spring Data Elasticsearch Documentation](https://docs.spring.io/spring-data/elasticsearch/docs/current/reference/html/)
- [Elasticsearch Aggregations](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-aggregations.html)

## 🔮 Próximos Pasos

1. **Migrar datos existentes** a la nueva estructura
2. **Implementar facets en frontend** para filtros dinámicos
3. **Agregar más tipos de agregaciones** según necesidades
4. **Optimizar índices** para mejor performance
5. **Implementar cache** para facets frecuentes
