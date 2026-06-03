-- Request Templates Table
CREATE TABLE IF NOT EXISTS request_templates (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    method TEXT NOT NULL,
    url TEXT NOT NULL,
    headers TEXT,
    body TEXT,
    auth_type TEXT,
    auth_config TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Test Scenarios Table
CREATE TABLE IF NOT EXISTS test_scenarios (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    type TEXT NOT NULL,
    description TEXT,
    config TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Scenario Steps Table
CREATE TABLE IF NOT EXISTS scenario_steps (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    scenario_id INTEGER NOT NULL,
    template_id INTEGER,
    step_order INTEGER NOT NULL,
    name TEXT,
    config TEXT,
    FOREIGN KEY (scenario_id) REFERENCES test_scenarios(id) ON DELETE CASCADE,
    FOREIGN KEY (template_id) REFERENCES request_templates(id) ON DELETE SET NULL
);
CREATE INDEX IF NOT EXISTS idx_scenario_steps_scenario_id ON scenario_steps(scenario_id);
CREATE INDEX IF NOT EXISTS idx_scenario_steps_template_id ON scenario_steps(template_id);

-- Global Parameters Table
CREATE TABLE IF NOT EXISTS global_params (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    type TEXT NOT NULL,
    value TEXT,
    scope TEXT,
    scenario_id INTEGER,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (scenario_id) REFERENCES test_scenarios(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_global_params_scenario_id ON global_params(scenario_id);

-- Test Tasks Table
CREATE TABLE IF NOT EXISTS test_tasks (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    scenario_id INTEGER NOT NULL,
    status TEXT NOT NULL DEFAULT 'pending',
    config TEXT,
    started_at DATETIME,
    completed_at DATETIME,
    result_summary TEXT,
    FOREIGN KEY (scenario_id) REFERENCES test_scenarios(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_test_tasks_scenario_id ON test_tasks(scenario_id);
