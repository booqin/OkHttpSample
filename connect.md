# Connect的创建

- ConnectionPool 维护RealConnection的Pool
- StreamAllocation 负责分配资源，获取RealConnection，创建Http2Codec等操作
- RealConnection 创建Socket，初始化OKIO的读（Source）和写(Sink)，以及Http2Connection等网络连接到操作。

ConnectionPool生命周期在整个OkHttpClient中，负责维护RealConnection的状态，StreamAllocation会在必要时创建/获取并使用RealConnection。

具体到一个请求中，以上会经历RetryAndFollowUpInterceptor =》 ConnectInterceptor =》CallServerInterceptor，其中

RetryAndFollowUpInterceptor维护了StreamAllocation的整个周期：

```java
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        StreamAllocation streamAllocation = new StreamAllocation(this.client.connectionPool(), this.createAddress(request.url()), call, eventListener, this.callStackTrace);
        ...
        response = realChain.proceed(request, streamAllocation, (HttpCodec)null, (RealConnection)null);
        ...
        streamAllocation.release();
    }
```

在proceed中向下传递，ConnectInterceptor中创建HttpCodec，同时获取或创建RealConnection：

```java
    public Response intercept(Chain chain) throws IOException {
        RealInterceptorChain realChain = (RealInterceptorChain)chain;
        ...
        StreamAllocation streamAllocation = realChain.streamAllocation();
        HttpCodec httpCodec = streamAllocation.newStream(this.client, chain, doExtensiveHealthChecks);
        RealConnection connection = streamAllocation.connection();
        return realChain.proceed(request, streamAllocation, httpCodec, connection);
    }
```

最后通过CallServerInterceptor去执行网络请求。