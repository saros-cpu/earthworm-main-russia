-- Update covers of existing packs that share generic images
UPDATE course_packs SET cover = 'https://images.unsplash.com/photo-1546410531-bb4caa6b424d?q=80&w=1200&auto=format&fit=crop' WHERE id = 'ru-basic-pack' AND cover = 'https://images.unsplash.com/photo-1513326738677-b964603b136d?q=80&w=1200&auto=format&fit=crop';

UPDATE course_packs SET cover = 'https://images.unsplash.com/photo-1503676260728-1c00da094a0b?q=80&w=1200&auto=format&fit=crop' WHERE id LIKE 'pdf-%' AND cover = 'https://images.unsplash.com/photo-1513326738677-b964603b136d?q=80&w=1200&auto=format&fit=crop';

UPDATE course_packs SET cover = 'https://images.unsplash.com/photo-1456513080510-7bf3a84b82f8?q=80&w=1200&auto=format&fit=crop' WHERE id = 'torfl-b1-ce9db7bd' AND cover = 'https://images.unsplash.com/photo-1513326738677-b964603b136d?q=80&w=1200&auto=format&fit=crop';

UPDATE course_packs SET cover = 'https://images.unsplash.com/photo-1497633762265-9d179a990aa6?q=80&w=1200&auto=format&fit=crop' WHERE id = 'torfl-b2-e526f4eb' AND cover = 'https://images.unsplash.com/photo-1513326738677-b964603b136d?q=80&w=1200&auto=format&fit=crop';

UPDATE course_packs SET cover = 'https://images.unsplash.com/photo-1513542789411-b6a5d4f31634?q=80&w=1200&auto=format&fit=crop' WHERE id = 'torfl-c1-9417e92f' AND cover = 'https://images.unsplash.com/photo-1495446815901-a7297e633e8d?q=80&w=1200&auto=format&fit=crop';

UPDATE course_packs SET cover = 'https://images.unsplash.com/photo-1455390587902-285d26427c2b?q=80&w=1200&auto=format&fit=crop' WHERE id = 'torfl-c2-b8cb9ae6' AND cover = 'https://images.unsplash.com/photo-1495446815901-a7297e633e8d?q=80&w=1200&auto=format&fit=crop';
