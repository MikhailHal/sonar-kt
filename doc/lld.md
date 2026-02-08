# Low-Level-Design
## Data Models
```kotlin
// 依存グラフのノード
data class MethodSignature(
    val className: String,
    val methodName: String,
    // オーバーロード区別
    val parameterTypes: List
)

// 依存グラフ
class DependencyGraph {
    private val edges: Map>
    
    fun getCallers(method: MethodSignature): Set<MethodSignature>
    fun getAllMethods(): Set<MethodSignature>
    fun isTestMethod(method: MethodSignature): Boolean
}
```