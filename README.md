# FireGramTV v6.1 — BOT Edition (Auto-Refresh)

- **Sin TDLib**: Bot API por long-polling (no necesitas servidor).
- **Auto-Refresh** del índice del bot al abrir la app (configurable en Ajustes).
- Ajustes: TMDB API Key, Bot Token, lista de canales.
- UI tipo Netflix (Home con tendencias, campañas estacionales, favoritos, recientes).
- Detalle de película/serie con temporadas/episodios.
- Reproducción por **streaming** desde Telegram (ExoPlayer). Soporta **multipart**.

## Compilar en GitHub
1) Sube este proyecto a tu repo.
2) Actions → **Build FireGramTV (BOT Debug)**.
3) Descarga el APK desde Artifacts.

## Nombres en Telegram
- Pelis: `Título (Año) {tmdb-12345}.mkv`
- Series: `Título (Año) S01E02 {tmdb-6789}.mkv` (o `01x02`)
- Multiparte: `part1`, `pt2`, `cd3`, `disc2`…

El bot debe ser **ADMIN** de tus canales privados.
