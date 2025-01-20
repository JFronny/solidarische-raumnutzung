CREATE TABLE soli_system_configuration
(
    id                     INT PRIMARY KEY CHECK (id = 1),
    guest_login_enabled    BOOLEAN NOT NULL,
    room_id                INT     NOT NULL,
    monday_opening_time    TIME,
    monday_closing_time    TIME,
    tuesday_opening_time   TIME,
    tuesday_closing_time   TIME,
    wednesday_opening_time TIME,
    wednesday_closing_time TIME,
    thursday_opening_time  TIME,
    thursday_closing_time  TIME,
    friday_opening_time    TIME,
    friday_closing_time    TIME,
    saturday_opening_time  TIME,
    saturday_closing_time  TIME,
    sunday_opening_time    TIME,
    sunday_closing_time    TIME,
    FOREIGN KEY (room_id) REFERENCES rooms (id)
);

INSERT INTO soli_system_configuration (id, guest_login_enabled, room_id)
VALUES (1, true, 1);