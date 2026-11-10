# suites.multiverse.no-snapshot (perf.yml)

**Que hace:** `NetworkTicker` (grabbers, pushers, celdas greedy) llamaba `Block.getState()` sobre cada contenedor vecino en
cada ciclo. En Paper eso crea un snapshot (copia del tile entity) solo para llegar al inventario. Con el flag usa
`getState(false)`: el mismo `InventoryHolder`, y para un bloque colocado `getInventory()` es el inventario vivo en ambos modos.

**Activar:** `drakes-data/perf.yml`
```yaml
suites:
  multiverse:
    no-snapshot: true   # default false
```
Se relee al inicio de cada ciclo del ticker (requiere kernel DrakesPurpur; en Paper plano queda false). Revertir = false.

**Medido:** NO medido. MockBukkit no copia tile entities, asi que un benchmark ahi no seria honesto. Prueba
`PerfFlagsTest`: con y sin flag el holder ve y modifica el mismo inventario; BlockFlows/GreedyCell siguen en verde.
Para medir en vivo: spark/timings del hilo principal con una red grande de grabbers/pushers sobre cofres, flag off vs on.

**Riesgos:** bajos; solo cambia como se obtiene el holder. Si algun contenedor exotico devolviera un inventario distinto
sin snapshot, se vera como items que no se mueven: poner el flag en false.
