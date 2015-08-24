package com.serenegiant.arflight;

import com.parrot.arsdk.arcontroller.ARFrame;

import java.nio.ByteBuffer;

/**
 * Created by saki on 2015/08/24.
 */
public class VideoStream {
	public ByteBuffer getCSD(final ARFrame frame) {
		int spsSize = -1;
		if (frame.isIFrame()) {
			final byte[] data = frame.getByteData();
			int searchIndex = 0;
			// we'll need to search the "00 00 00 01" pattern to find each header size
			// Search start at index 4 to avoid finding the SPS "00 00 00 01" tag
			for (searchIndex = 4; searchIndex <= frame.getDataSize() - 4; searchIndex ++) {
				if (0 == data[searchIndex  ] &&
						0 == data[searchIndex+1] &&
						0 == data[searchIndex+2] &&
						1 == data[searchIndex+3])
				{
					break;  // PPS header found
				}
			}
			spsSize = searchIndex;

			// Search start at index 4 to avoid finding the PSS "00 00 00 01" tag
			for (searchIndex = spsSize+4; searchIndex <= frame.getDataSize() - 4; searchIndex ++) {
				if (0 == data[searchIndex  ] &&
						0 == data[searchIndex+1] &&
						0 == data[searchIndex+2] &&
						1 == data[searchIndex+3]) {
					break;  // frame header found
				}
			}
			int csdSize = searchIndex;

			final byte[] csdInfo = new byte[csdSize];
			System.arraycopy(data, 0, csdInfo, 0, csdSize);
			return ByteBuffer.wrap(csdInfo);
		}
		return null;
	}
}
