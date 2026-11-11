# Synthetic OSM road fixture

`synthetic-roads.osm` is invented test data, created for uetlib and distributed under the repository's Apache-2.0
license. It contains no downloaded OpenStreetMap data, real users or production infrastructure.

Seven nodes and three ways describe a main road (1–7–2–3), a side road (2–4) and a disconnected path (5–6).
Node 2 is an intersection; node 7 is an intermediate geometry point collapsed by routing-graph construction.
The side road carries `oneway=yes` to test tag preservation. The raw parser creates undirected edges; this fixture
makes no claim that it enforces one-way restrictions.

Tests read the committed PBF directly, offline. To regenerate it after editing the XML, run from the repository root:

```sh
uv run --with osmium==4.3.0 python - <<'PY'
import osmium
from pathlib import Path
base = Path('uetlib-geo/src/test/resources/osm')
with osmium.SimpleWriter(str(base / 'synthetic-roads.osm.pbf'), overwrite=True) as writer:
    for obj in osmium.FileProcessor(str(base / 'synthetic-roads.osm')):
        writer.add(obj)
PY
```

PyOsmium is needed only for fixture generation, not for Maven builds or tests.
