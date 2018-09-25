## Cache

主要的类：

- Cache
- DiskLRUCache
- CacheStrategy

缓存策略

将大量的response判断移至strategy（CacheControl判断生成CacheStrategy）

OkHttp默认提供对Get请求的缓存功能，对于其它Post，HEAD等虽然技术上能实现，但是过于复杂，效益很低。

> Don't cache non-GET responses. We're technically allowed to cache
  HEAD requests and some POST requests, but the complexity of doing
  so is high and the benefit is low.

内部的缓存由DiskLRUCache维护，以URL为Key。Cache实现了InternalCache接口，该接口提供基础的「增删改查」操作

### 保存到数据Body

okhttp中返回的response中，数据在其内部的responseBody对象中，可根据来源分为两种实现为RealResponseBody和CacheResponseBody，
两者都继承自ResponseBody类。两者的区别在于Cache获取source的途径来自于DiskLRUCache的Snapshot类。

在第一次请求数据过程中：

1. 根据当前配置（CacheControl）生成CacheStrategy，对应的networkRequest和cacheResponse
2. request和cache都无效的情况下返回504的response
3. networkRequest为空时，即缓存还在存活期内，返回cacheResponse
4. 向下一个Intercept传递请求，从网络获取新的response
5. 更新/新增缓存到DiskLRUCache

新增缓存的过程，在构建Response时创建一个通过cacheWritingResponse方法封装了cacheWritingSource的RealResponseBody，在body被读取到过程中会同步保存到DiskLRUCache中。需要了解okio中source和sink的知识。

```java
  private Response cacheWritingResponse(final CacheRequest cacheRequest, Response response)
      throws IOException {
    // Some apps return a null body; for compatibility we treat that like a null cache request.
    ...
    Source cacheWritingSource = new Source() {
      boolean cacheRequestClosed;

      @Override public long read(Buffer sink, long byteCount) throws IOException {
        long bytesRead;
        try {
          bytesRead = source.read(sink, byteCount);
        } catch (IOException e) {
          if (!cacheRequestClosed) {
            cacheRequestClosed = true;
            cacheRequest.abort(); // Failed to write a complete cache response.
          }
          throw e;
        }

        if (bytesRead == -1) {
          if (!cacheRequestClosed) {
            cacheRequestClosed = true;
            cacheBody.close(); // The cache response is complete!
          }
          return -1;
        }
        // 复制数据到DiskLRUCache中
        sink.copyTo(cacheBody.buffer(), sink.size() - bytesRead, bytesRead);
        cacheBody.emitCompleteSegments();
        return bytesRead;
      }

      ...
      return response.newBuilder()
          .body(new RealResponseBody(contentType, contentLength, Okio.buffer(cacheWritingSource)))
          .build();
    };
```



