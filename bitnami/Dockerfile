FROM docker.io/redislabs/redisearch:latest as redisearch
FROM bitnami/redis:latest

COPY --from=redisearch /usr/lib/redis/modules/redisearch.so /usr/lib/redis/modules/redisearch.so

CMD ["/run.sh", "--loadmodule", "/usr/lib/redis/modules/redisearch.so"]