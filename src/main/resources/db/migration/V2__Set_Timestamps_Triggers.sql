CREATE TRIGGER set_created_updated
AFTER INSERT ON todos
FOR EACH ROW
BEGIN
    UPDATE todos
    SET created_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP
    WHERE id = NEW.id;
END;

CREATE TRIGGER set_updated
AFTER UPDATE ON todos
FOR EACH ROW
BEGIN
    UPDATE todos
    SET updated_at = CURRENT_TIMESTAMP
    WHERE id = NEW.id;
END;
