package sj.zheng.mdimg;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.ImageUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Objects;

public class PasteAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent event) {
        Image image = ImageUtils.getImage();
        Editor ed = event.getData(PlatformDataKeys.EDITOR);
        if (ed == null || image == null) {
            return;
        }
        Document currentDoc = Objects.requireNonNull(FileEditorManager.getInstance(Objects.requireNonNull(ed.getProject())).getSelectedTextEditor())
                                     .getDocument();
        VirtualFile currentFile = FileDocumentManager.getInstance().getFile(currentDoc);
        assert currentFile != null;
        BufferedImage bufferedImage;
        File curDocument = new File(currentFile.getPath());
        if (image instanceof BufferedImage) {
            bufferedImage = (BufferedImage) image;
        } else {
            int w = image.getWidth(null);
            int h = image.getHeight(null);
            if (w < 0 || h < 0) {
                return;
            }
            bufferedImage = ImageUtil.createImage(w, h, BufferedImage.TYPE_INT_ARGB);
        }
        File imageDir = new File(curDocument.getParent(), "img");
        if (!imageDir.exists()) {
            imageDir.mkdirs();
        }
        String imageName = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now()) + ".png";
        File imageFile = new File(imageDir, imageName);
        try {
            ImageIO.write(bufferedImage, "png", imageFile);
        } catch (Exception e) {
            return;
        }
        File relFile = curDocument.getParentFile().toPath().relativize(imageFile.toPath()).toFile();
        String relImagePath = relFile.toString().replace('\\', '/');
        WriteCommandAction.runWriteCommandAction(ed.getProject(), () -> EditorModificationUtil.insertStringAtCaret(ed, "![" + imageName + "](" + relImagePath + ")"));
        VirtualFile fileByPath = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(imageFile);
        assert fileByPath != null;
        ApplicationManager.getApplication().invokeLater(() -> {
            AbstractVcs usedVcs = ProjectLevelVcsManager.getInstance(ed.getProject()).getVcsFor(fileByPath);
            if (usedVcs != null && usedVcs.getCheckinEnvironment() != null) {
                ApplicationManager.getApplication().executeOnPooledThread(() -> {
                    usedVcs.getCheckinEnvironment().scheduleUnversionedFilesForAddition(Collections.singletonList(fileByPath));
                });
            }
        });
    }
}
