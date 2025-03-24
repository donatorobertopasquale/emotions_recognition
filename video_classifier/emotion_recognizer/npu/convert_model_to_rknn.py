import numpy as np
import cv2
import torch
from rknn.api import RKNN
import os
import sys

# Add the parent directory to the path to import your model
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
# append also a diretory behind this file
sys.path.append(os.path.dirname(os.path.abspath(__file__)))
from ResEmoteNet import ResEmoteNet  # Import your model class - adjust import path as needed

def trace_model():
    print('--> Tracing PyTorch model')
    # Load your original model
    model = ResEmoteNet()  # Adjust constructor arguments as needed
    model.load_state_dict(torch.load('./ResEmoteNetBS32.pth', map_location='cpu'))
    model.eval()
    
    # Create dummy input
    dummy_input = torch.randn(1, 3, 64, 64)
    
    # Trace the model
    traced_model = torch.jit.trace(model, dummy_input)
    
    # Save the traced model
    traced_model.save('./ResEmoteNetBS32.pt')
    print('Model traced and saved as .pt file')
    return './ResEmoteNetBS32.pt'

if __name__ == '__main__':
    os.chdir(os.path.dirname(__file__))

    # First trace and export the model to .pt format
    traced_model_path = trace_model()
    
    input_size_list = [[1, 3, 64, 64]]

    # Create RKNN object
    rknn = RKNN(verbose=True)

    # Pre-process config
    print('--> Config model')
    rknn.config(
        mean_values=[[0.485*255, 0.456*255, 0.406*255]],  # Match your normalization values
        std_values=[[0.229*255, 0.224*255, 0.225*255]],   # Multiply by 255 for 0-255 range
        target_platform='rk3588'
    )
    print('done')

    # Load traced model
    print('--> Loading model')
    ret = rknn.load_pytorch(model=traced_model_path, input_size_list=input_size_list)
    if ret != 0:
        print('Load model failed!')
        exit(ret)
    print('done')
    
    # Build model
    print('--> Building model')
    ret = rknn.build(do_quantization=False)
    if ret != 0:
        print('Build model failed!')
        exit(ret)
    print('done')

    # Export rknn model
    print('--> Export rknn model')
    ret = rknn.export_rknn('./ResEmoteNetBS32.rknn')
    if ret != 0:
        print('Export rknn model failed!')
        exit(ret)
    print('done')

    rknn.release()