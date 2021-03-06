package de.alpharogroup.mystic.crypt.panels.privatekey;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.codec.DecoderException;

import de.alpharogroup.model.BaseModel;
import de.alpharogroup.model.api.Model;
import de.alpharogroup.mystic.crypt.panels.keygen.EnDecryptPanel;
import de.alpharogroup.swing.base.BasePanel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.miginfocom.swing.MigLayout;


@Getter
@Slf4j
public class PrivateKeyPanel extends BasePanel<PrivateKeyModelBean>
{

	private static final long serialVersionUID = 1L;

	private PrivateKeyViewPanel privateKeyViewPanel;

	private EnDecryptPanel enDecryptPanel;

	public PrivateKeyPanel()
	{
		this(BaseModel.<PrivateKeyModelBean> of(PrivateKeyModelBean.builder().build()));
	}

	public PrivateKeyPanel(final Model<PrivateKeyModelBean> model)
	{
		super(model);
	}

	/**
	 * Callback method that can be overwritten to provide specific action for the on decrypt.
	 *
	 * @param actionEvent
	 *            the action event
	 */
	protected void onDecrypt(final ActionEvent actionEvent)
	{
		System.out.println("onDecrypt");
		try
		{
			final String decryted = getModelObject().getDecryptor()
				.decrypt(getEnDecryptPanel().getTxtEncrypted().getText());
			getEnDecryptPanel().getTxtToEncrypt().setText(decryted);
			getEnDecryptPanel().getTxtEncrypted().setText("");
		}
		catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
			| IllegalBlockSizeException | BadPaddingException | InvalidKeySpecException
			| InvalidAlgorithmParameterException | DecoderException | IOException e)
		{
			log.error("", e);
		}

	}

	/**
	 * Callback method that can be overwritten to provide specific action for the on encrypt.
	 *
	 * @param actionEvent
	 *            the action event
	 */
	protected void onEncrypt(final ActionEvent actionEvent)
	{
		System.out.println("onEncrypt");
		try
		{
			getEnDecryptPanel().getTxtEncrypted().setText(getModelObject().getEncryptor()
				.encrypt(getEnDecryptPanel().getTxtToEncrypt().getText()));
			getEnDecryptPanel().getTxtToEncrypt().setText("");
		}
		catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException
			| NoSuchPaddingException | UnsupportedEncodingException e)
		{
			log.error("", e);
		}
		catch (final IllegalBlockSizeException e)
		{
			log.error("", e);
		}
		catch (final BadPaddingException e)
		{
			log.error("", e);
		}
		catch (final IOException e)
		{
			log.error("", e);
		}

	}

	@Override
	protected void onInitializeComponents()
	{
		super.onInitializeComponents();
		privateKeyViewPanel = new PrivateKeyViewPanel();

		enDecryptPanel = new EnDecryptPanel()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onDecrypt(final ActionEvent actionEvent)
			{
				PrivateKeyPanel.this.onDecrypt(actionEvent);
			}

			@Override
			protected void onEncrypt(final ActionEvent actionEvent)
			{
				PrivateKeyPanel.this.onEncrypt(actionEvent);
			}
		};
	}

	@Override
	protected void onInitializeLayout()
	{
		super.onInitializeLayout();
		onInitializeMigLayout();
	}


	/**
	 * Initialize layout.
	 */
	protected void onInitializeMigLayout()
	{
		setLayout(new MigLayout());
		add(privateKeyViewPanel, "wrap");
		add(enDecryptPanel);
	}

}
