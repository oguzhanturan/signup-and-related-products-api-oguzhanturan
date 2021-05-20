package school.cactus.succulentshop.product.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import school.cactus.succulentshop.R
import school.cactus.succulentshop.api.api
import school.cactus.succulentshop.api.product.Product
import school.cactus.succulentshop.api.product.RelatedProducts
import school.cactus.succulentshop.databinding.FragmentProductDetailBinding
import school.cactus.succulentshop.product.detail.relatedproducts.RelatedProductAdapter
import school.cactus.succulentshop.product.detail.relatedproducts.RelatedProductDecoration
import school.cactus.succulentshop.product.toProductItem
import school.cactus.succulentshop.product.toProductItemList

class ProductDetailFragment : Fragment() {
    private var _binding: FragmentProductDetailBinding? = null

    private val binding get() = _binding!!

    private val args: ProductDetailFragmentArgs by navArgs()

    private val adapter = RelatedProductAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().title = getString(R.string.app_name)

        binding.relatedProductRecyclerView.adapter = adapter
        binding.relatedProductRecyclerView.layoutManager =
            StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL)
        binding.relatedProductRecyclerView.addItemDecoration(RelatedProductDecoration())

        adapter.itemClickListener = {
            val action = ProductDetailFragmentDirections.openProductDetailFragmentSelf(it.id)
            findNavController().navigate(action)
        }
        fetchProduct()
        fetchRelatedProduct()

        fetchProduct()
    }

    private fun fetchRelatedProduct() {
        api.getRelatedProductById(args.productId).enqueue(object : Callback<RelatedProducts> {
            override fun onResponse(
                call: Call<RelatedProducts>,
                response: Response<RelatedProducts>
            ) {
                when (response.code()) {
                    200 -> onSuccessRelatedProducts(response.body()!!)
                    401 -> onTokenExpired()
                    else -> onUnexpectedError()
                }
            }

            override fun onFailure(call: Call<RelatedProducts>, t: Throwable) {
                Snackbar.make(
                    binding.root, R.string.check_your_connection,
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(R.string.retry) {
                    fetchRelatedProduct()
                }.show()
            }
        })
    }

    private fun onSuccessRelatedProducts(relatedProducts: RelatedProducts) {
        if (relatedProducts.products.isNotEmpty()) {
            adapter.submitList(relatedProducts.products.toProductItemList())
        } else {
            binding.relatedProductsText.visibility = View.GONE
            binding.relatedProductRecyclerView.visibility = View.GONE
        }
    }

    private fun fetchProduct() {
        api.getProductById(args.productId).enqueue(object : Callback<Product> {
            override fun onResponse(call: Call<Product>, response: Response<Product>) {
                when (response.code()) {
                    200 -> onSuccess(response.body()!!)
                    401 -> onTokenExpired()
                    else -> onUnexpectedError()
                }
            }

            override fun onFailure(call: Call<Product>, t: Throwable) {
                Snackbar.make(
                    binding.root, R.string.check_your_connection,
                    Snackbar.LENGTH_INDEFINITE
                ).setAction(R.string.retry) {
                    fetchProduct()
                }.show()
            }
        })
    }

    private fun onSuccess(product: Product) {
        val productItem = product.toProductItem()
        binding.apply {
            titleText.text = productItem.title
            priceText.text = productItem.price
            descriptionText.text = productItem.description

            Glide.with(binding.root)
                .load(productItem.highResImageUrl)
                .into(imageView)
        }
        binding.progressBar2.setZ(1F);
        binding.progressBar2.setVisibility(View.GONE);
    }

    private fun onTokenExpired() {
        Snackbar.make(
            binding.root, R.string.your_session_is_expired,
            Snackbar.LENGTH_INDEFINITE
        ).setAction(R.string.log_in) {
            navigateToLogin()
        }.show()
    }

    private fun navigateToLogin() = findNavController().navigate(R.id.tokenExpired)

    private fun onUnexpectedError() {
        Snackbar.make(
            binding.root, R.string.unexpected_error_occurred,
            BaseTransientBottomBar.LENGTH_LONG
        ).show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}