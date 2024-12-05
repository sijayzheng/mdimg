package sj.zheng.mdimg;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

public class ImageUtils {

    public static Image getImage() {
        Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        try {
            if (transferable == null || transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                return null;
            }
            if (transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                return (Image) transferable.getTransferData(DataFlavor.imageFlavor);
            } else {
                return null;
            }
        } catch (Exception e) {
            System.out.println("发生错误：" + e.getMessage());
            return null;
        }
    }
}
