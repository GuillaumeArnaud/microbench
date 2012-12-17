package fr.xebia.microbench.internals

import java.lang.management.CompilationMXBean
import java.lang.management.GarbageCollectorMXBean

import static java.lang.Math.round
import static java.lang.Runtime.getRuntime
import static java.lang.management.ManagementFactory.*

class Collectors {

    static Closure<Map<String, Object>> detailedCollector = {
        def collectedData = defaultCollector.call()
        collectedData["gc"] = [:]
        garbageCollectorMXBeans.each { GarbageCollectorMXBean bean ->
            collectedData["gc"][bean.name] = [:]
            collectedData["gc"][bean.name]["count"] = bean.collectionCount
            collectedData["gc"][bean.name]["time"] = bean.collectionTime
            if (bean instanceof com.sun.management.GarbageCollectorMXBean) {
                collectedData["gc"][bean.name]["duration"] = ((com.sun.management.GarbageCollectorMXBean) bean).lastGcInfo?.duration
            }
        }
        collectedData
    }

    static Closure<Map<String, Object>> defaultCollector = {
        Map<String, Object> collectedData = new HashMap<String, Object>()
        collectedData["memory"] = [
                "used (%)": round(100 * (getRuntime().totalMemory() - getRuntime().freeMemory()) / getRuntime().totalMemory()),
                "total (Byte)": getRuntime().totalMemory() - getRuntime().freeMemory(),
                "used (Byte)": getRuntime().totalMemory() - getRuntime().freeMemory()
        ]
        collectedData["thread"] = ["thread": Thread.activeCount()]
        collectedData["class"] = [
                "loaded (current)": classLoadingMXBean.loadedClassCount,
                "loaded (total)": classLoadingMXBean.totalLoadedClassCount
        ]
        collectedData["compilation"] = [:]
        compilationMXBean.each { CompilationMXBean compilationMXBean ->
            collectedData["compilation"][compilationMXBean.name] = ["total (ms)": compilationMXBean.totalCompilationTime]
        }
        collectedData
    } as Closure<Map<String, Object>>


}
