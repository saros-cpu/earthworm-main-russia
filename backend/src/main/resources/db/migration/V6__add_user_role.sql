ALTER TABLE `users` ADD COLUMN `role` VARCHAR(32) NOT NULL DEFAULT 'USER' AFTER `email`;
UPDATE `users` SET `role` = 'ADMIN' WHERE `username` = 'yangjie';
DELETE FROM `users` WHERE `username` = 'dev';
