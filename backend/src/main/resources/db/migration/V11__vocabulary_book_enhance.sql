ALTER TABLE vocabulary_book
  ADD COLUMN part_of_speech VARCHAR(50) NULL AFTER chinese,
  ADD COLUMN phonetic VARCHAR(255) NULL AFTER part_of_speech,
  ADD COLUMN example_sentence TEXT NULL AFTER phonetic,
  ADD COLUMN example_translation TEXT NULL AFTER example_sentence,
  ADD COLUMN study_level INT NOT NULL DEFAULT 0 AFTER example_translation;
