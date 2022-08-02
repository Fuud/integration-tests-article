# Сквозное и интеграционное тестирование просто, как юнит-тесты.

Когда изменения затрагивают несколько микросервисов, возникает вопрос, как протестировать. Можно покрыть границы
сервисов юнит тестами, а интеграцию проверить, развернув измененный код на тестовом окружении. У такого подхода две
главные проблемы: цикл изменения-тестирование-исправления становится достаточно долгим и нужно много полноценных
окружений, чтобы обеспечить параллельную работу нескольких разработчиков. Давайте попробуем решить проблему иначе.

Будем решать проблему последовательно, по шагам преодолевая возникающие трудности.
Код можно найти на [GitHub](https://github.com/Fuud/integration-tests-article). К каждой части будет ссылка на ревизию.

## 1. Постановка задачи

[rev:2b8fcd50](https://github.com/Fuud/integration-tests-article/commit/2b8fcd509151ebc83c24d2b4e9fd0b665eb82ded)

Для примера возьмем два микросервиса, написанных с использованием spring-boot. Для простоты у нас будет многомодульный
мавен-проект с двумя севисами: client-service и worker-service. Допустим, что надо реализовать функционал:

client-service должен принимать http запросы с задачами и отправлять их на выполнение в worker-service, а worker-service
возвращает идентификатор выполняемой задачи.

Получилось два эндпоинта:

ClientServiceEndpoint

```java
    @PostMapping("/task")
public String placeTask(@RequestBody ClientRequest request){
        return restTemplate.postForObject(config.getWorkerUrl(),request,WorkerResponseDto.class).getJobId();
        }
```

WorkerServiceEndpoint

```java
    @PostMapping("/task")
public WorkerResponseDto placeTask(@RequestBody ClientRequest request){
        WorkerResponseDto workerResponseDto=new WorkerResponseDto();
        workerResponseDto.setJobId(UUID.randomUUID().toString());
        return workerResponseDto;
        }
```

Оба проекта можно запустить локально (`ClientServiceApplication.main` и `WorkerServiceApplication.main`). Теперь можно к
ним написать мануальные тесты или кодом или в какой-нибудь специализированной среде вроде Talend Api Tester.

Этот подход работает. Его даже можно автоматизировать, если отдельными шагами запускать приложения. Но при автоматизации
можно столкнуться со следующими трудностями:

1. Сложно следить за запущенными приложениями (надо не забыть их остановить после тестов)
2. Если микросервисов много, то их придется все запускать руками при разработке (или прогонять всю сборку целиком
   мавеном/гредлом)
3. Если у приложений есть состояние, то тесты могут начать влиять друг на друга.
4. Сложно тестировать сценарии деградации при недоступности одного из микросервисов. Перекликается с пунктом 1 и 3: если
   остановить один из компонентов, могут упасть тесты, использующие этот компонент. Надо после каждого теста
   восстановить исходное состояние (и не запускать тесты одновременно)

Можно сформулировать требования к идеальным межкомпонентным тестам:

1. Должны запускаться одной кнопкой run test из IDE
2. Написание не должно представлять трудностей и не должно сильно отличаться от написания юнит-тестов.
3. Должны поддерживать отладку отдельных микросервисов
4. Должны быть изолированы друг от друга
5. Запуск теста должен быть достаточно быстрый, чтобы при разработке можно было пользоваться практикой TDD
6. Должны быть интегрированы c CI. Идеально, чтобы их можно было прогонять при проверке пул реквестов.

Давайте попробуем решить задачу тестирования с учетом этих требований.

## 2. Пробуем наивное решение: сделаем модуль, зависимый от модулей микросервисов и напишем тест в нем.

[rev:cfbebf68](https://github.com/Fuud/integration-tests-article/commit/cfbebf68c0876dc2bfaaca8cb3074d7c6275d414)

Первое, что приходит в голову когда надо протестировать функциональность двух микросервисов в связке, это сделать третий
модуль для тестов, зависимый от сервисных модулей. Попробуем написать тест в нем:

```java
public class TaskIntegrationTest {
   @Test
   public void testTaskSubmission() throws Exception {
      ClientServiceApplication.main(new String[0]);
      WorkerServiceApplication.main(new String[0]);

      HttpResponse<String> response = HttpClient.newBuilder().build().send(
                HttpRequest.newBuilder()
                        .method("POST", HttpRequest.BodyPublishers.ofString("{ \"data\":\"my-data\"}"))
                        .header("Content-Type", "application/json")
                        .uri(URI.create("http://localhost:8080/task"))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(response.statusCode(), 200);
        assertFalse(response.body().isBlank());
    }
}
```

Но такой тест не заработает. Причина: у нашего теста в класспасе оказалось два application.yml и Spring берет первый
попавшийся. Исправим это, задав имена приложений. Например, для client-service назовем файл конфигурации
application-client.yml и зададим имя так:

```java
    public static void main(String[]args){
        SpringApplication.run(ClientServiceApplication.class,"--spring.config.name=application-client");
        }
```

Если у всех наших приложений одинаковый набор зависимостей, то такой способ подойдет. Надо только доделать закрытие
спрингового контекста и выбор свободных портов.

## 3. Добавление зависимостей в один из сервисов затрагивает другие сервисы в тестах

[rev:7c8abae7](https://github.com/Fuud/integration-tests-article/commit/7c8abae738f827b7601bc42704c3c1e657ae09fb)

Если у разных сервисов разный набор зависимостей, то тесты могут вести себя непредсказуемым образом. Например, если мы
хотим защитить client-service и добавляем spring-boot-starter-security в зависимости, то неожиданно оказывается
защищенным и worker-service. И тесты падают несмотря на то, что production build у worker-service не поменялся. Можно
предположить и существование обратного случая: тесты проходят, а на реальном окружении что-то не работает.

Вывод: чтобы тестировать микросервисы надо запускать каждый из них с тем же класспасом, что будет использован в боевом
окружении.

## 4. Используем maven-dependency-plugin, чтобы получить правильный класспас

[rev:50d2802f](https://github.com/Fuud/integration-tests-article/commit/50d2802f9f4cbf710beb65fbd87850139b3131d6)

В этой части речь пойдет про maven. Для gradle можно сделать примерно так же.

Чтобы получить правильный список зависимостей в правильном порядке можно
вызвать `mvn compile dependency:build-classpath`. Здесь включение фазы compile обязательно, потому что иначе мавен будет
считать внутрипроектные зависимости внешними и пытаться найти их в .m2 и внешних репозиториях. Подробности
тут: [MNG-3283](https://issues.apache.org/jira/browse/MNG-3283).

Далее вопрос в том, кто будет вызывать dependency-plugin? Есть следующий варианты:

1. Прописываем в pom.xml, локально вызываем из командной строки, на CI вызовется автоматически.
2. Используем [maven-embedder](https://maven.apache.org/ref/3.8.4/maven-embedder/) и вызываем прямо из теста. Проблема в
   том, что у maven-embedder нет собранной версии с зависимостями, а тянет за собой он очень много. И это с легкостью
   ломает тесты. Но можно его собрать и положить в свой репозиторий.

Мне кажется, что достаточно первого варианта. Дописываем в pom.xml

```xml

<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-dependency-plugin</artifactId>
    <version>3.3.0</version>
    <executions>
        <execution>
            <id>generate classpath file for IT</id>
            <goals>
                <goal>build-classpath</goal>
            </goals>
            <phase>process-classes</phase>
            <configuration>
                <includeScope>runtime</includeScope>
                <outputFile>${project.build.directory}/classpath_${project.artifactId}.txt</outputFile>
            </configuration>
        </execution>
    </executions>
</plugin>
```

Теперь после исполнения `mvn process-classes` в target окажутся файлы со списком зависимостей. Не составит труда их
найти и прочитать, если знать, где находится корень проекта. Проблема тут в том, что текущая директория при запуске из
IDE и при запуске maven-surefire-plugin может отличаться. Но в любом случае она находится внутри проекта. Поэтому можно
положить файл-маркер рядом с самым верхним pom.xml, искать его вверх, а потом от него рекурсивно спускаться.

```java
    private static File findTopProjectDir()throws IOException{
        File topProjectDir=new File(".").getCanonicalFile();
        do{
        if(new File(topProjectDir,".top.project.dir").exists()){
        return topProjectDir;
        }
        topProjectDir=topProjectDir.getParentFile();
        }while(topProjectDir!=null);

        throw new IllegalStateException("Cannot find marker file .top.project.dir starting from "+new File(".").getAbsolutePath());
        }
```

И вычитать класспасы, складывая их в Map по ключу artifact_id (если у вас artifact_id не уникальный, можно использовать
group_id:artifact_id). Особенность тут заключается в том, что build_classpath не включает target/classes того модуля,
для которого класспасс строится, эту директорию надо добавить дополнительно в начало classpath:

```java
    private static void searchForClassPathFiles(File topProjectDir,Map<String, List<String>>results)throws IOException{
        File pomXml=new File(topProjectDir,"pom.xml");
        if(pomXml.exists()){
        File targetDir=new File(topProjectDir,"target");
        File[]classPathFiles=targetDir.listFiles(pathname->pathname.getName().startsWith("classpath_")&&pathname.getName().endsWith(".txt"));
        if(classPathFiles!=null){
        if(classPathFiles.length>1){
        throw new IllegalStateException("Found more than one classpath file in dir "+targetDir.getAbsolutePath());
        }
        if(classPathFiles.length==1){
        File classPathFile=classPathFiles[0];
        List<String> classPath=new ArrayList<>(Arrays.asList(Files.readString(classPathFile.toPath()).split(System.getProperty("path.separator"))));
        // maven-dependency-plugin build-classpath does not include module classes, let's include them now
        classPath.add(0,new File(targetDir,"classes").getAbsolutePath());

        String artifactId=classPathFile.getName().replaceAll("^classpath_","").replaceAll(".txt$","");
        if(results.containsKey(artifactId)){
        throw new IllegalStateException("Duplicate artifact id: "+artifactId);
        }
        results.put(artifactId,classPath);
        }
        }
        File[]probablySubmodules=topProjectDir.listFiles(File::isDirectory);
        if(probablySubmodules!=null){
        for(File probablySubmodule:probablySubmodules){
        searchForClassPathFiles(probablySubmodule,results);
        }
        }
        }
        }
```

На CI все пройдет хорошо - файлы со списком зависимостей будут актуальными. А вот в локальной разработке сложно не
забыть обновить файлы после изменения зависимостей в pom.xml. Чтобы программно заметить изменения, я предлагаю все
pom.xml при сборке скопировать в target. Именно все, потому что прослеживать внутремодульные зависимости сложно:

```xml

<plugin>
    <groupId>com.coderplus.maven.plugins</groupId>
    <artifactId>copy-rename-maven-plugin</artifactId>
    <version>1.0</version>
    <executions>
        <execution>
            <id>copy-pom</id>
            <phase>process-classes</phase>
            <goals>
                <goal>copy</goal>
            </goals>
            <configuration>
                <sourceFile>pom.xml</sourceFile>
                <destinationFile>target/pom-copy.xml</destinationFile>
            </configuration>
        </execution>
    </executions>
</plugin>
```

И сравнить их с оригиналами перед вычитыванием classpath files.

```java
    private static void checkPomChanges(File topProjectDir)throws IOException{
        File pomXml=new File(topProjectDir,"pom.xml");
        if(pomXml.exists()){
        File targetPomFile=new File(new File(topProjectDir,"target"),"pom-copy.xml");
        if(!targetPomFile.exists()){
        throw new IllegalStateException(targetPomFile.getAbsolutePath()+" is not generated, run `mvn process-classes` first");
        }
        if(!Files.readString(pomXml.toPath()).equals(Files.readString(targetPomFile.toPath()))){
        throw new IllegalStateException(targetPomFile.getAbsolutePath()+" is not equal to "+pomXml.getAbsolutePath()+", run `mvn process-classes` first");
        }
        File[]probablySubmodules=topProjectDir.listFiles(File::isDirectory);
        if(probablySubmodules!=null){
        for(File probablySubmodule:probablySubmodules){
        checkPomChanges(probablySubmodule);
        }
        }
        }
        }
```

## 5. Запускаем сервисы в отдельных процессах используя библиотеку nanocloud

[rev:f9118159](https://github.com/Fuud/integration-tests-article/commit/f9118159e514d15b9897104ec7b47e69b9e0c63d)

Теперь, зная classpath, можно запустить сервисы в отдельных процессах. Запуск в одном процессе, но разных класслоадерах
скорее всего приведет к трудностям, так как разные библиотеки используют разных общий стейт: системные переменные,
Service Providers и другие возможности, которые приходят с boot class loader.

Запуск в отдельной jvm можно сделать с помощью ProcessBuilder. А можно воспользоваться
библиотекой [nanocloud](https://github.com/gridkit/nanocloud). Вот так можно запустить сервис:

```java
        Cloud cloud=CloudFactory.createCloud();
        ViNode clientNode=cloud.node("client");
        clientNode.x(VX.CLASSPATH).inheritClasspath(false);
        ViProps.at(clientNode).setLocalType(); // будем запускать локально, в отдельной jvm
        ClassPathHelper.getClasspathForArtifact("client-service")
        .forEach(classPathElement->clientNode.x(VX.CLASSPATH).add(classPathElement));
        clientNode.exec(new Runnable(){
@Override
public void run(){
        ClientServiceApplication.main(new String[0]);
        }
        });
```

## 6. Добавляем обертки для ViNode, заменяем анонимные классы на лямбды

[rev:f7e1724b](https://github.com/Fuud/integration-tests-article/commit/f7e1724b9e8c35d976c8912c07444f1228af16b5)

При запуске сервиса был использован анонимный класс, а не лямбда. Это было сделано потому, что nanocloud для пересылки
объектов использует java-serialization с дополнением для сериализации анонимных классов. Это было удобно в java 1.6, но
сейчас выглядит архаично. Но если просто заменить анонимный класс на лямбду, то произойдет ошибка сериализации. Поэтому
удобно написать обертку (заодно научив ее различать callable и runnable):

```java
public class Node implements ViConfigurable {

   private final ViNode node;

   public Node(Cloud cloud, String name) {
      node = cloud.node(name);
    }

    public void exec(SerializableRunnable runnable) {
        node.exec(runnable);
    }

    public <T> T execAndReturn(SerializableCallable<T> callable) {
        return node.exec(callable);
    }
    
    ...
    и другие
    методы,
    делегирующие к
    ViNode

   public interface SerializableRunnable extends Runnable, Serializable {
      void run();
   }

   public interface SerializableCallable<T> extends Callable<T>, Serializable {
      T call();
   }
}    
```

## 7. Выделяем свободные порты сервисам.

[rev:90080a1c](https://github.com/Fuud/integration-tests-article/commit/90080a1c51519a0bbf2126dda1b01fc69730db99)

Сейчас сервисы запускаются на портах, которые прописаны в файлах конфигурации. У этого есть два недостатка: порт может
быть занят другим приложением и нельзя запускать тесты в параллель.

Самый простой способ получить свободный порт такой:

```java
    int freePort()throws Exception{
        try(ServerSocket socket=new ServerSocket(0)){
        return socket.getLocalPort();
        }
        }
```

Недостатки:

1. Один и тот же порт может быть выдан несколько раз (пока приложение не запустится и не заберет порт себе)
2. Порты будут разные от запуска к запуску, что усложняет отладку (например, при каждом перезапуске теста придется
   вводить новый адрес, если мы что-то тестируем в ручном режиме)

Можно поступить так: сперва найдем свободный базовый порт, займем его, а потому будем раздавать последовательно порты,
начиная со следующего за базовым.

```java
public class PortAllocator {
    private static final int CHUNK_SIZE = 10_000;
    // сохраняем ServerSocket в поле, чтобы он не был прибран ГЦ и 
    // другой запуск тестов не мог забрать базовый порт
    private static ServerSocket basePortHolder;
    private static int port;

    public static synchronized int freePort() {
        if (basePortHolder == null) {
            for (int i = 1; i < 6; i++) {
                try {
                    basePortHolder = new ServerSocket(i * CHUNK_SIZE);
                    break;
                } catch (IOException e) {
                    // ignore
                }
            }
            if (basePortHolder == null) {
                throw new IllegalStateException("Cannot find port base, all ports are occupied");
            }
            port = basePortHolder.getLocalPort();
        }
        // ищем следующий свободный порт
        while (port < basePortHolder.getLocalPort() + CHUNK_SIZE) {
            port++;
            if (portIsFree(port)) {
                return port;
            }
        }
        throw new IllegalStateException("Cannot find free port starting from " + basePortHolder.getLocalPort());
    }

    private static boolean portIsFree(int port) {
        try {
            // next line better than just new ServerSocket(port), 
            // check https://github.com/spring-projects/spring-framework/issues/17906 for discussion
            try (ServerSocket ignored = new ServerSocket(port, 0, InetAddress.getByName("localhost"))) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }
}
```

Теперь нужно раздать порты сервисам, а client-service еще должен узнать порт worker-service. Можно выделить абстрактную
обертку над сервисом.

```java
public interface Component {
    /**
     * Этот метод будет запущен для старта компонента
     * @param env список всех компонентов в текущем тесте
     */
    void start(Cloud cloud, List<Component> env);
}
```

Тогда в тесте можно будет писать вот так:

```java
        Cloud cloud=CloudFactory.createCloud();
        ClientComponent clientComponent=new ClientComponent();
        // стартуем компоненты
        env(
        cloud,
        clientComponent,
        new WorkerComponent()
        );
```

И метод env будет таким:

```java
    public static void env(Cloud cloud,Component...components){
        for(Component component:components){
        component.start(cloud,Arrays.asList(components));
        }
        }
```

Тогда компоненты смогут сами найти порты тех сервисов, которые им нужны. В нашем случае обертка для client-service будет
выглядеть так:

```java
public class ClientComponent implements Component {
    public static class Config {
        public final int restPort = PortAllocator.freePort();
    }

    public Config config = new Config();

    @Override
    public void start(Cloud cloud, List<Component> env) {
        Node clientNode = new Node(cloud, "client");
        clientNode.x(VX.CLASSPATH).inheritClasspath(false);
        ViProps.at(clientNode).setLocalType();
        ClassPathHelper.getClasspathForArtifact("client-service")
                .forEach(classPathElement -> clientNode.x(VX.CLASSPATH).add(classPathElement));

        // здесь мы используем порт
        clientNode.x(VX.JVM).setEnv("server.port", config.restPort + "");

       // здесь мы ищем WorkerService и передаем его порт в переменные окружения
       WorkerComponent worker = findComponent(env, WorkerComponent.class);
       clientNode.x(VX.JVM).setEnv("client-service.worker-url", "http://localhost:" + worker.config.restPort);

       clientNode.exec(() -> ClientServiceApplication.main(new String[0]));
    }
}
```

## 8. Распечатываем конфиги

[rev:a020e1f9](https://github.com/Fuud/integration-tests-article/commit/a020e1f92ea9e6fdce97671a598b3ecd32ffa93b)

У нас появился произвол в выборе портов, поэтому полезно их сразу распечатывать в консоль. Для http портов лучше
распечатывать сразу вместе со ссылкой для быстрого запуска в браузере. Для этого параметризуем компоненты классом
конфига, сложим все конфиги в словарь по имени компонента и распечатаем.

```java
public abstract class Component<TConfig> {
   protected final TConfig config;

   public Component(TConfig config) {
      this.config = config;
   }

    abstract public void start(Cloud cloud, List<Component<?>> env);

    public TConfig getConfig() {
        return config;
    }
}
```

```java
public class WorkerComponent extends Component<WorkerComponent.Config> {
    public static class Config {
        final int restPort = PortAllocator.freePort();
        private final String link = "http://localhost:" + restPort;
    }
    ...
}
```

```java
public class EnvStarter {
    public static void env(Cloud cloud, Component<?>... components) {
        printConfigsToConsole(components);

        for (Component<?> component : components) {
            component.start(cloud, Arrays.asList(components));
        }
    }

    private static void printConfigsToConsole(Component<?>[] components) {
        ObjectWriter objectWriter = new ObjectMapper()
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                .writerWithDefaultPrettyPrinter();

        Map<String, Object> configMap = new HashMap<>();
        for (Component<?> component : components) {
            configMap.put(component.getClass().getSimpleName(), component.getConfig());
        }

       try {
          System.out.println(objectWriter.writeValueAsString(configMap));
       } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
       }
    }
}
```

## 9. Останавливаем сервисы после теста

[rev:7b4e07f9](https://github.com/Fuud/integration-tests-article/commit/7b4e07f9f78a290adc1ffc64cfd4c953780e20c3)

Библиотека nanocloud следит, чтобы запущенные ею инстансы jvm были остановлены после остановки jvm, на которой был
создан Cloud. Но если мы запустим много тестов в одной jvm (так делает, например, maven-surefire-plugin по умолчанию),
то запущенные сервисы будут остановлены только после того, как все тесты пройдут. Надо их явно останавливать после
теста. Можно это решить с помощью, например, [JUnit Rules](https://www.baeldung.com/junit-4-rules). А можно завернуть
тест в лямбду:

```java

@FunctionalInterface
public interface TestBlock {
   void performTest(Cloud cloud) throws Exception;
}

   public static void integrationTest(TestBlock block) {
        Cloud cloud = CloudFactory.createCloud();
        try {
            block.performTest(cloud);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            cloud.shutdown();
        }
    }
```

И тест тогда будет выглядеть так:

```java
    @Test
public void testTaskSubmission(){
        integrationTest((cloud)->{
        ClientComponent clientComponent=new ClientComponent();
        env(cloud,clientComponent,new WorkerComponent());

        // do the test
        });
        }
```

## 10. Ускоряем тесты. Настаиваем параллелизацию и конфигурим jvm на быстрый старт

[rev:3cfefd2c](https://github.com/Fuud/integration-tests-article/commit/3cfefd2cebc9c9a75d93c15fcfe5d0671756340c)

Если запустить 100 таких тестов, то выполнение на моем ноутбуке займет примерно 8-9 минут.

Если выполнять тесты в два потока, то выполнение займет 4-5 минут. Дальнейшее увеличение количества потоков _на моем
ноутбуке_ прироста к скорости не дает. Так что настроим два потока:

```xml

<plugin>
   <groupId>org.apache.maven.plugins</groupId>
   <artifactId>maven-surefire-plugin</artifactId>
    <version>3.0.0-M7</version>
    <configuration>
        <argLine>--add-opens java.base/jdk.internal.loader=ALL-UNNAMED</argLine>
        <parallel>classesAndMethods</parallel>
        <threadCount>2</threadCount>
    </configuration>
</plugin>
```

Нам нужен быстрый старт. Похожую задачу решают те, кто пишет для serverless,
например, [Optimizing AWS Lambda function performance for Java](https://aws.amazon.com/ru/blogs/compute/optimizing-aws-lambda-function-performance-for-java/)
. Вроде бы лучше всего ускоряют следующие аргументы: `-XX:TieredStopAtLevel=1 -Xverify:none`. Задаем эти параметры для
всех сервисов:

```java
    private static void applyCommonJvmArgs(Cloud cloud){
        ViNode allNodes=cloud.node("**"); // ** значит все ноды
        allNodes.x(VX.JVM).addJvmArg("-XX:TieredStopAtLevel=1");
        allNodes.x(VX.JVM).addJvmArg("-Xverify:none");
        }
```

Время выполнения становится 1-2 минуты.

## 11. Включаем отладку для сервисов [rev:2b327516](https://github.com/Fuud/integration-tests-article/commit/2b3275160afa6da7928ff6bc8efaa2b458fc600c)

Чтобы отлаживать сервисы из IDE, нам надо:

1. выбрать порт: делаем общего предка для всех конфигов и выбираем порт так же, как выбирали порт для http:

```java
    public static class BaseComponentConfig {
    public final int debugPort = PortAllocator.freePort();
}
```

2. удобно подключаться к сервисам: если пользуемся IntelliJ Idea, то достаточно в консоль
   вывести `Listening for transport dt_socket at address: 8888` и рядом с надписью появится кнопочка Attach Debugger.
   Добавляем линку в конфиг (пробел в конце обязателен!).

```java
    public static class BaseComponentConfig {
    public final int debugPort = PortAllocator.freePort();
    public final String debugLink = "Listening for transport dt_socket at address: " + debugPort + " ";
}
```

3. включать дебаг только когда нужно. Я нашел вариант на [StackOverflow](https://stackoverflow.com/a/71375505). Спасибо
   @apangin.

```java
    private static boolean detectIsDebugEnabled(){
        ThreadInfo[]infos=ManagementFactory.getThreadMXBean()
        .dumpAllThreads(false,false,0);
        for(ThreadInfo info:infos){
        if("JDWP Command Reader".equals(info.getThreadName())){
        return true;
        }
        }
        return false;
        }
```

4. настраивать дебаг для всех сервисов в одном месте. Заменим Cloud на наш интерфес NodeProvider и получим такой код для
   сетапа теста:

```java

@FunctionalInterface
public interface NodeProvider {
    Node getNode(String name, Component.BaseComponentConfig config);
}


@FunctionalInterface
public interface TestBlock {
    void performTest(NodeProvider nodeProvider) throws Exception;
}

    public static void integrationTest(TestBlock block) {
        Cloud cloud = CloudFactory.createCloud();
        applyCommonJvmArgs(cloud);
        NodeProvider nodeProvider = (name, config) -> {
            Node node = new Node(cloud, name);
            if (isDebugEnabled) {
                node.x(VX.JVM).addJvmArgs("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:" + config.debugPort);
            }
            return node;
        };
       try {
          block.performTest(nodeProvider);
       } catch (Exception e) {
          throw new RuntimeException(e);
       } finally {
          cloud.shutdown();
       }
    } 
```

## 12. Запускаем сервисы на удаленной машине по ssh

[rev:dcf68675](https://github.com/Fuud/integration-tests-article/commit/dcf6867527c32eb8c4d9c9119148e7be14216d00)

Если тесты долгие или потребляют много ресурсов, можно запускать сервисы на удаленной машине. Настроить nanocloud
запускать сервисы по ssh очень просто. Можно авторизоваться по паролю, можно по ключу:

```java
    private static void configureRemoteExecution(ViNode allNodes){
        RemoteNode remoteNodeConfig=allNodes.x(RemoteNode.REMOTE);
        remoteNodeConfig.setRemoteNodeType();

        // выключаем загрузку ключей/хостов из конфиг файла, 
        // все будем настраивать явно в коде
        remoteNodeConfig.setHostsConfigFile("?na");

        remoteNodeConfig.setRemoteAccount(System.getProperty("int.tests.remote.user"));
        remoteNodeConfig.setPassword(System.getProperty("int.tests.remote.password"));
//        remoteNodeConfig.setSshPrivateKey(System.getProperty("int.tests.remote.key.path"));
        remoteNodeConfig.setRemoteHost(System.getProperty("int.tests.remote.host"));
        remoteNodeConfig.setRemoteJarCachePath("nanocloud-cache"); // куда складывать jar файлы
        remoteNodeConfig.setRemoteJavaExec(System.getProperty("int.tests.remote.java")); // где искать java
        }
```

Теперь можно передать правильное имя хоста в тест и тест, скорее всего пройдет. "Скорее всего", потому что, свободные
порты мы ищем локально с тестом, а http сервер запускаем удаленно.

Для того чтобы выделять порты правильно, надо выполнять `PortAllocator.freePort` удаленно. Удобно это сделать с помощью
nanocloud transparent rmi. Работает он следующим образом: если класс реализует интерфейс, который наследуется от Remote,
то при сериализации вместо класса будет отправлен прокси, реализующий этот интерфейс.

В нашем случае:

```java
public interface PortAllocator extends Remote {
    int freePort();
}

class PortAllocatorImpl implements PortAllocator {
    @Override
    public synchronized int freePort() { ...}
}

    private static PortAllocator obtainPortAllocatorFromRemoteNode() {
        Cloud serviceCloud = CloudFactory.createCloud();
        Node serviceNode = new Node(serviceCloud, "service-node");
        // настраиваем ноду на выполнение по ssh
        configureRemoteExecution(serviceNode);
        // создаем PortAllocatorImpl удаленно и получаем локальный прокси
        return serviceNode.execAndReturn(PortAllocatorImpl::new);
    }
```

При использовании transparent rmi надо быть осторожным с типами. Например, вот такой код упадет с ClassCastException,
потому что после сериализации-десериализации прилетит прокси, реализующее интерфейс, а не сам объект.

```java
// java.lang.ClassCastException: class jdk.proxy2.$Proxy11 cannot be cast to class fuud.test.infra.PortAllocator$PortAllocatorImpl
PortAllocatorImpl portAllocator=serviceNode.execAndReturn(PortAllocator.PortAllocatorImpl::new);
```

Правильно так:

```java
PortAllocator portAllocator=serviceNode.execAndReturn(PortAllocator.PortAllocatorImpl::new);
```

## 13. Заключение и советы по дальнейшему использованию и развитию

Я показал, как можно построить фреймворк для интеграционного тестирования. Если вы начнете его использовать, возможно,
вам будет не хватать каких-то возможностей. Все варианты использования описать сложно, предлагаю наметки по тем
сценариям, которые мне встречались:

1. Тестирование обратной совместимости: для проверки взаимодействия компонентов разных версий достаточно положить в
   classpath собранные в предыдущий релиз артефакты (вместо классов из target).
2. Если для сценариев нужна база, очередь сообщений или что-то разрабатываемое другой командой, рекомендую посмотреть
   на [TestContainers](https://www.testcontainers.org/).
3. Очередь сообщений можно так же эмулировать, найдя все бины с, например, @KafkaListener и дергая их через transparent
   rmi.
4. Для тестирования проблем с сетью можно
   использовать [Sniffy](https://www.sniffy.io/docs/latest/#_testing_bad_connectivity) или написать обертку
   для [netem](https://wiki.linuxfoundation.org/networking/netem).

Такой подход не заменит юнит-тесты хотя бы потому, что такие тесты занимают заметное время. С другой стороны, они могут
обеспечить более стабильный мастер, отловив ошибки на ранних стадиях. А стабильный мастер - это спокойные нервы и
крепкий сон по ночам.

