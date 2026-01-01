<?php
/**
 * Query Builder Helper
 * Simplifies building dynamic SQL queries with filters and pagination
 */

class QueryBuilder {
    private PDO $db;
    private string $table;
    private string $alias;
    private array $select = ['*'];
    private array $joins = [];
    private array $where = [];
    private array $params = [];
    private array $orderBy = [];
    private ?int $limit = null;
    private ?int $offset = null;
    
    public function __construct(PDO $db, string $table, string $alias = '') {
        $this->db = $db;
        $this->table = $table;
        $this->alias = $alias ?: substr($table, 0, 1);
    }
    
    /**
     * Set SELECT columns
     */
    public function select(array $columns): self {
        $this->select = $columns;
        return $this;
    }
    
    /**
     * Add a JOIN clause
     */
    public function join(string $table, string $condition, string $type = 'LEFT'): self {
        $this->joins[] = "$type JOIN $table ON $condition";
        return $this;
    }
    
    /**
     * Add a WHERE condition
     */
    public function where(string $condition, ...$params): self {
        $this->where[] = $condition;
        foreach ($params as $param) {
            $this->params[] = $param;
        }
        return $this;
    }
    
    /**
     * Add WHERE condition if value is not null/empty
     */
    public function whereIf(?string $value, string $condition, $transform = null): self {
        if ($value !== null && $value !== '') {
            $this->where[] = $condition;
            $this->params[] = $transform ? $transform($value) : $value;
        }
        return $this;
    }
    
    /**
     * Add WHERE condition for integer if value is not null
     */
    public function whereInt(?string $value, string $condition): self {
        if ($value !== null && $value !== '') {
            $this->where[] = $condition;
            $this->params[] = (int)$value;
        }
        return $this;
    }
    
    /**
     * Add WHERE condition for float if value is not null
     */
    public function whereFloat(?string $value, string $condition): self {
        if ($value !== null && $value !== '') {
            $this->where[] = $condition;
            $this->params[] = (float)$value;
        }
        return $this;
    }
    
    /**
     * Add LIKE search condition across multiple columns
     */
    public function whereSearch(?string $search, array $columns): self {
        if ($search !== null && $search !== '') {
            $conditions = array_map(fn($col) => "$col LIKE ?", $columns);
            $this->where[] = "(" . implode(' OR ', $conditions) . ")";
            $searchTerm = "%$search%";
            foreach ($columns as $_) {
                $this->params[] = $searchTerm;
            }
        }
        return $this;
    }
    
    /**
     * Add ORDER BY clause
     */
    public function orderBy(string $column, string $direction = 'ASC'): self {
        $this->orderBy[] = "$column $direction";
        return $this;
    }
    
    /**
     * Set pagination
     */
    public function paginate(int $page, int $perPage): self {
        $this->limit = $perPage;
        $this->offset = ($page - 1) * $perPage;
        return $this;
    }
    
    /**
     * Build the SQL query
     */
    private function buildQuery(bool $forCount = false): string {
        $alias = $this->alias;
        $selectClause = $forCount ? 'COUNT(*)' : implode(', ', $this->select);
        
        $sql = "SELECT $selectClause FROM {$this->table} $alias";
        
        if (!empty($this->joins)) {
            $sql .= ' ' . implode(' ', $this->joins);
        }
        
        if (!empty($this->where)) {
            $sql .= ' WHERE ' . implode(' AND ', $this->where);
        }
        
        if (!$forCount) {
            if (!empty($this->orderBy)) {
                $sql .= ' ORDER BY ' . implode(', ', $this->orderBy);
            }
            
            if ($this->limit !== null) {
                $sql .= " LIMIT {$this->limit}";
                if ($this->offset !== null) {
                    $sql .= " OFFSET {$this->offset}";
                }
            }
        }
        
        return $sql;
    }
    
    /**
     * Execute query and return results
     */
    public function get(): array {
        $sql = $this->buildQuery();
        $stmt = $this->db->prepare($sql);
        $stmt->execute($this->params);
        return $stmt->fetchAll();
    }
    
    /**
     * Execute query and return single result
     */
    public function first(): ?array {
        $this->limit = 1;
        $results = $this->get();
        return $results[0] ?? null;
    }
    
    /**
     * Get total count (ignoring LIMIT/OFFSET)
     */
    public function count(): int {
        $sql = $this->buildQuery(true);
        $stmt = $this->db->prepare($sql);
        $stmt->execute($this->params);
        return (int)$stmt->fetchColumn();
    }
    
    /**
     * Execute paginated query and return results with pagination info
     */
    public function getPaginated(int $page, int $perPage): array {
        // Get total count first (before applying pagination)
        $total = $this->count();
        
        // Then get paginated results
        $this->paginate($page, $perPage);
        $results = $this->get();
        
        return [
            'data' => $results,
            'pagination' => [
                'page' => $page,
                'per_page' => $perPage,
                'total' => $total,
                'total_pages' => (int)ceil($total / $perPage)
            ]
        ];
    }
    
    /**
     * Get params for debugging
     */
    public function getParams(): array {
        return $this->params;
    }
    
    /**
     * Get SQL for debugging
     */
    public function toSql(): string {
        return $this->buildQuery();
    }
}

/**
 * Helper function to create a new QueryBuilder instance
 */
function query(string $table, string $alias = ''): QueryBuilder {
    return new QueryBuilder(getDB(), $table, $alias);
}

/**
 * Parse pagination parameters from request
 * Standardizes on 'page' and 'per_page' parameters
 */
function getPaginationParams(int $defaultPerPage = 20, int $maxPerPage = 50): array {
    $page = max(1, (int)(getQueryParam('page') ?? 1));
    // Support both 'per_page' and 'limit' for backwards compatibility
    $perPage = getQueryParam('per_page') ?? getQueryParam('limit') ?? $defaultPerPage;
    $perPage = min($maxPerPage, max(1, (int)$perPage));
    
    return [$page, $perPage];
}

/**
 * Standard paginated response format
 */
function paginatedSuccessResponse(array $data, string $key, array $pagination): void {
    successResponse([
        $key => $data,
        'pagination' => $pagination
    ]);
}
