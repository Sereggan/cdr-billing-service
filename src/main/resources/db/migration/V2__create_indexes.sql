CREATE INDEX IF NOT EXISTS idx_customer_billing_cycles_cycle_total_cost
    ON customer_billing_cycles (cycle, total_cost DESC, customer_id);
