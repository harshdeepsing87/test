@Configuration
public class RestClient {
	@Value("${httpclient.maxtotalconnection}")
	private int maxTotalConnection;
	@Value("${httpclient.readtimeoutmillisecods}")
	private int readTimeoutMilliSecond;
	@Value("${httpclient.apimaxconnection}")
	private int apiMaxConnection;
	@Value("${httpclient.stsmaxconnection}")
	private int stsMaxConnection;
	@Value("${host.sts.uri}")
	private String stsUri;
	@Value("${host.api.host}")
	private String apiUri;
	@Value("${async.connectTimeOutMilliSec}")
	private int connectTimeOut;
	@Value("${async.readTimeOutMilliSec}")
	private int readTimeOut;
	@Value("${async.connectRequestTimeoutMilliSec}")
	private int connectRequestTimeout;

	@Bean
	public RestTemplate restTemplate() {
		return configureRestTemplate(new RestTemplate(httpRequestFactory()));
	}

	private CloseableHttpClient httpClient() {
		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
		connectionManager.setMaxTotal(maxTotalConnection);
		connectionManager.setMaxPerRoute(new HttpRoute(new HttpHost(stsUri)), stsMaxConnection);
		RequestConfig config = RequestConfig.custom().setConnectTimeout(readTimeoutMilliSecond).build();
		return HttpClientBuilder.create().setConnectionManager(connectionManager).setDefaultRequestConfig(config).build();
	}

	private ClientHttpRequestFactory httpRequestFactory() {
		return new HttpComponentsClientHttpRequestFactory(httpClient());
	}

	@Bean
	public AsyncRestTemplate asyncRestTemplate() {
		return new AsyncRestTemplate(asyncHttpRequestFactory(), configureRestTemplate(new RestTemplate()));
	}

	private CloseableHttpAsyncClient asyncHttpClient() {
		try {
			PoolingNHttpClientConnectionManager connectionManager = new PoolingNHttpClientConnectionManager(new DefaultConnectingIOReactor(IOReactorConfig.DEFAULT));
			connectionManager.setMaxTotal(maxTotalConnection);
			connectionManager.setMaxPerRoute(new HttpRoute(new HttpHost(apiUri)), apiMaxConnection);
			RequestConfig config = RequestConfig.custom().setConnectTimeout(readTimeoutMilliSecond).build();
			return HttpAsyncClientBuilder.create().setConnectionManager(connectionManager).setDefaultRequestConfig(config).build();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private RestTemplate configureRestTemplate(RestTemplate restTemplate) {
		List<HttpMessageConverter<?>> converters = restTemplate.getMessageConverters();
		for (HttpMessageConverter<?> converter : converters) {
			if (converter instanceof MappingJackson2HttpMessageConverter) {
				MappingJackson2HttpMessageConverter jsonConverter = (MappingJackson2HttpMessageConverter) converter;
				jsonConverter.setObjectMapper(new com.fasterxml.jackson.databind.ObjectMapper());
			}
		}
		return restTemplate;
	}

	private AsyncClientHttpRequestFactory asyncHttpRequestFactory() {
		HttpComponentsAsyncClientHttpRequestFactory asyncReqFactory = new HttpComponentsAsyncClientHttpRequestFactory(asyncHttpClient());
		asyncReqFactory.setConnectionRequestTimeout(connectRequestTimeout);
		asyncReqFactory.setConnectTimeout(connectTimeOut);
		asyncReqFactory.setReadTimeout(readTimeOut);
		return asyncReqFactory;
	}

}
