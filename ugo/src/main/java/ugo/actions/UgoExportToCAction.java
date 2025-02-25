package ugo.actions;

/* ###
 * IP: GHIDRA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import docking.action.ToolBarData;
import docking.widgets.OptionDialog;
import docking.widgets.filechooser.GhidraFileChooser;
import ghidra.app.decompiler.ClangTokenGroup;
import ghidra.app.decompiler.DecompiledFunction;
import ghidra.app.decompiler.PrettyPrinter;
import ghidra.app.plugin.core.decompile.DecompilerActionContext;
import ghidra.framework.preferences.Preferences;
import ghidra.util.Msg;
import ghidra.util.filechooser.ExtensionFileFilter;
import resources.ResourceManager;
import ugo.UgoDecompilerController;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class UgoExportToCAction extends UgoAbstractDecompilerAction {
    private static final ImageIcon EXPORT_ICON = ResourceManager.loadImage("images/page_edit.png");
    private static final String LAST_USED_C_FILE = "last.used.decompiler.c.export.file";
    private final UgoDecompilerController controller;

    public UgoExportToCAction(UgoDecompilerController controller) {
        super("Export to C");
        this.controller = controller;
        setToolBarData(new ToolBarData(EXPORT_ICON, "Local"));
        setDescription("Export the current function to C");
    }

    private File readLastUsedFile() {
        String filename = Preferences.getProperty(LAST_USED_C_FILE);
        if (filename == null) {
            return null;
        }
        return new File(filename);
    }

    private void saveLastUsedFileFile(File file) {
        Preferences.setProperty(LAST_USED_C_FILE, file.getAbsolutePath());
        Preferences.store();
    }

    private File getFile() {
        File lastUsedFile = readLastUsedFile();

        String[] extensions = new String[]{"h", "c", "cpp"};
        GhidraFileChooser fileChooser = new GhidraFileChooser(controller.getDecompilerPanel());
        fileChooser.setFileFilter(new ExtensionFileFilter(extensions, "C/C++ Files"));
        if (lastUsedFile != null) {
            fileChooser.setSelectedFile(lastUsedFile);
        }
        File file = fileChooser.getSelectedFile();
        if (file == null) {
            return null;
        }

        saveLastUsedFileFile(file);

        boolean hasExtension = false;
        String path = file.getAbsolutePath();
        for (String element : extensions) {
            if (path.toLowerCase().endsWith("." + element)) {
                hasExtension = true;
            }
        }

        if (!hasExtension) {
            file = new File(path + ".c");
        }
        return file;
    }

    @Override
    protected boolean isEnabledForDecompilerContext(DecompilerActionContext context) {
        return controller.getFunction() != null && controller.getCCodeModel() != null;
    }

    @Override
    protected void decompilerActionPerformed(DecompilerActionContext context) {

        File file = getFile();
        if (file == null) {
            return;
        }

        if (file.exists()) {
            if (OptionDialog.showYesNoDialog(controller.getDecompilerPanel(),
                    "Overwrite Existing File?",
                    "Do you want to overwrite the existing file?") == OptionDialog.OPTION_TWO) {
                return;
            }
        }

        try {
            PrintWriter writer = new PrintWriter(new FileOutputStream(file));
            ClangTokenGroup grp = controller.getCCodeModel();
            PrettyPrinter printer = new PrettyPrinter(controller.getFunction(), grp);
            DecompiledFunction decompFunc = printer.print(true);
            writer.write(decompFunc.getC());
            writer.close();
            controller.setStatusMessage(
                    "Successfully exported function(s) to " + file.getAbsolutePath());
        } catch (IOException e) {
            Msg.showError(getClass(), controller.getDecompilerPanel(), "Export to C Failed",
                    "Error exporting to C: " + e);
        }
    }
}
