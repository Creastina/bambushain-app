package app.bambushain.finalfantasy.characters;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import dagger.hilt.android.lifecycle.HiltViewModel;

import javax.inject.Inject;

@HiltViewModel
public class CharacterListViewModel extends ViewModel {
    public final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    @Inject
    public CharacterListViewModel() {
    }
}