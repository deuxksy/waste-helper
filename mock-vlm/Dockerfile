FROM python:3.12-slim

WORKDIR /app

# proto 파일 복사 및 stub 생성
COPY vlm_service.proto .
RUN pip install --no-cache-dir grpcio grpcio-tools flask && \
    python -m grpc_tools.protoc \
        --proto_path=. \
        --python_out=. \
        --grpc_python_out=. \
        vlm_service.proto

COPY app.py .

EXPOSE 8000 50051
CMD ["python", "app.py"]
