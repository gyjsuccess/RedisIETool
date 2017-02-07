package com.cxy.redisclient.presentation.favorite;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import com.cxy.redisclient.integration.I18nFile;
import com.cxy.redisclient.presentation.RedisClient;
import com.cxy.redisclient.presentation.component.RedisClientDialog;

public class AddFavoriteDialog extends RedisClientDialog {

	private String container;
	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public AddFavoriteDialog(Shell parent, Image image, String container) {
		super(parent, image);
		this.container = container;
	}

	/**
	 * Create contents of the dialog.
	 */
	protected void createContents() {
		shell.setText(RedisClient.i18nFile.getText(I18nFile.ADDFAVORITE));
		shell.setLayout(new GridLayout(1, false));
		
		TabFolder tabFolder = new TabFolder(shell, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		TabItem tbtmString = new TabItem(tabFolder, SWT.NONE);
		tbtmString.setText(RedisClient.i18nFile.getText(I18nFile.FAVORITE));
		
		Composite composite = new Composite(tabFolder, SWT.NONE);
		tbtmString.setControl(composite);
		composite.setLayout(new GridLayout(2, false));
		
		Label lblNewKey = new Label(composite, SWT.NONE);
		lblNewKey.setText(RedisClient.i18nFile.getText(I18nFile.NAME));
		
		final Text text_2 = new Text(composite, SWT.BORDER);
		text_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		text_2.setText(container==null?"": container);
		text_2.selectAll();
		text_2.setFocus();
		
		Composite composite_1 = new Composite(shell, SWT.NONE);
		composite_1.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, false, true, 1, 1));
		composite_1.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		Button button = new Button(composite_1, SWT.NONE);
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				String name = text_2.getText();
				
				if( name.length() == 0){
					MessageDialog.openError(shell, RedisClient.i18nFile.getText(I18nFile.ERROR),RedisClient.i18nFile.getText(I18nFile.INPUTFAVORITE));
				} else {
					result = name;
					shell.dispose();
				}
			}
		});
		button.setText(RedisClient.i18nFile.getText(I18nFile.OK));
		
		Button button_1 = new Button(composite_1, SWT.NONE);
		button_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				shell.dispose();
			}
		});
		button_1.setText(RedisClient.i18nFile.getText(I18nFile.CANCEL));
		
		super.createContents();
	}
}
