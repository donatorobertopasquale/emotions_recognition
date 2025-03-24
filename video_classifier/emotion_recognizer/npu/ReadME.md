git clone https://github.com/airockchip/rknn-toolkit2.git
cd rknn-tolkit2/rknn-toolkit2/packages/arm64
install requirements and .whl file based on your python version

then you can run convert_model_to_rknn.py to convert the model to run on opi5 NPU

sudo sh -c "curl https://raw.githubusercontent.com/airockchip/rknn-toolkit2/refs/heads/master/rknpu2/runtime/Linux/librknn_api/aarch64/librknnrt.so > /usr/lib64/librknnrt.so"