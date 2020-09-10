package com.imaxcorp.imaxc.providers

import android.content.Context
import android.graphics.Bitmap
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.imaxcorp.imaxc.services.CompressorBitmapImage
import java.io.ByteArrayOutputStream
import java.io.File

class ImagesProvider(ref: String) {
    private var mStorage: StorageReference? = null

    init {
        if (mStorage==null){
            mStorage = FirebaseStorage.getInstance().reference.child(ref)
        }
    }

    fun saveImage(mContext: Context, image: File, idUser: String): UploadTask? {
        val imageByte = CompressorBitmapImage.getImage(mContext,image.path,500,500)
        val storage = mStorage?.child("$idUser.jpg")
        mStorage = storage
        return storage?.putBytes(imageByte)
    }

    fun getStorage(): StorageReference? {
        return mStorage
    }

    fun saveBitmap(mContext: Context, image: Bitmap, idDocument: String) : UploadTask? {
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG,100, baos)
        val data = baos.toByteArray()

        val storage = mStorage?.child(idDocument)
        mStorage = storage
        return storage?.putBytes(data)
    }
}